package com.juneqqq.util;

import cn.hutool.core.codec.Base64Decoder;
import com.alibaba.fastjson.JSONObject;
import com.juneqqq.entity.exception.CustomException;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class ImageUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String BAIDU_TOKEN_KEY = "baidu-access-token";

    @Value("${baidu.develop.auth.url}")
    private String authUrl;

    @Value("${baidu.develop.clientId}")
    private String clientId;

    @Value("${baidu.develop.clientSecret}")
    private String clientSecret;

    @Value("${baidu.develop.splitBody.url}")
    private String splitBodyUrl;

    public BufferedImage getBodyOutline(BufferedImage image, InputStream inputStream) throws Exception {
        //调用百度api进行人像分割
        String base64 = this.bodySeg(inputStream);
        JSONObject resultJson = JSONObject.parseObject(base64);
        //将结果转换为黑白剪影（二值图）
        return this.convert(resultJson.getString("labelmap"), image.getWidth(), image.getHeight());
    }

    public String bodySeg(InputStream inputStream) throws Exception{
        log.debug("开始请求百度人体分割api");
        long start = System.currentTimeMillis();
        String imgStr = this.convertFileToBase64(inputStream);
        String accessToken = stringRedisTemplate.opsForValue().get(BAIDU_TOKEN_KEY);
        if(StringUtil.isNullOrEmpty(accessToken)){
            accessToken = this.getAuth();
            stringRedisTemplate.opsForValue().set(BAIDU_TOKEN_KEY, accessToken);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("image", imgStr);
        splitBodyUrl += "?access_token=" + accessToken;
        HttpUtil.HttpResponse result = HttpUtil.postUrlEncoded(splitBodyUrl, params);
        log.debug("请求结束，总时间(s)为：" +( (System.currentTimeMillis() - start)/ 1000F));
        if(result.getBody().contains("error_msg")){
            log.error("错误信息：{}",result.getBody());
            throw new CustomException("百度图像分割错误，信息如上");
        }
        return result.getBody();
    }

    /**
     * 灰度图转为纯黑白图
     */
    public BufferedImage convert(String labelmapBase64, int realWidth, int realHeight) {
        try {
            byte[] bytes = Base64.getDecoder().decode(labelmapBase64);
            InputStream is = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(is);
            BufferedImage newImage = resize(image, realWidth, realHeight);
            BufferedImage grayImage = new BufferedImage(realWidth, realHeight, BufferedImage.TYPE_BYTE_BINARY);
            for (int i = 0; i < realWidth; i++) {
                for (int j = 0; j < realHeight; j++) {
                    int rgb = newImage.getRGB(i, j);
                    grayImage.setRGB(i, j, rgb * 255);  //将像素存入缓冲区
                }
            }
            return grayImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static InputStream BaseToInputStream(String base64string){
        ByteArrayInputStream stream = null;
        try {
            byte[] bytes = Base64Decoder.decode(base64string.getBytes(StandardCharsets.UTF_8));
            stream = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stream;
    }

    public static BufferedImage base64String2BufferedImage(String base64string) {
        BufferedImage image = null;
        try {
            InputStream stream = BaseToInputStream(base64string);
            image = ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 压缩图片
     */
    public static String resizeImage(String base64Img, Integer maxSize) {
        String base64 = null;
        try {
            if (base64Img.length() - base64Img.length() / 8 * 2 <= maxSize) {
                return base64Img;
            }
            //图片过大则进行压缩
            BufferedImage src = base64String2BufferedImage(base64Img);
            //等比例压缩
//            BufferedImage output = Thumbnails.of(src).size(0.8).asBufferedImage();
            //将原图的长宽缩小至原来的三分之一进行压缩
             BufferedImage output = Thumbnails.of(src).size(src.getWidth()/3, src.getHeight()/3).asBufferedImage();
            base64 = imageToBase64(output);
            if (base64.length() - base64.length() / 8 * 2 > maxSize) {
                output = Thumbnails.of(output).scale(1/(base64.length()/maxSize)).asBufferedImage();
                base64 = imageToBase64(output);
            }
            return base64;
        } catch (Exception e) {
            log.error("压缩失败！");
            e.printStackTrace();
            return base64Img;
        }
//        return resizeImage(base64, maxSize);
    }

    public static String imageToBase64(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode((baos.toByteArray())));
    }


    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    public String convertFileToBase64(InputStream inputStream) {
        byte[] data = null;
        // 读取图片字节数组
        try {
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组进行Base64编码，得到Base64编码的字符串
//        BASE64Encoder encoder = new BASE64Encoder();
//        return encoder.encode(data);
        return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
    }

    /**
     * 百度获取Token
     */
    private String getAuth() throws Exception{
        // 获取token地址
        URL realUrl = new URL(authUrl + "?grant_type=client_credentials" + "&client_id=" + clientId + "&client_secret=" + clientSecret);
        // 打开和URL之间的连接
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        // 定义 BufferedReader输入流来读取URL的响应
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result = "";
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        return jsonObject.getString("access_token");
    }

    public void transferAlpha(File file, File outputFile) throws Exception{
        InputStream is = new FileInputStream(file);
        Image image = ImageIO.read(is);
        ImageIcon imageIcon = new ImageIcon(image);
        BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
        g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
        int alpha = 0;
        for (int j1 = bufferedImage.getMinY(); j1 < bufferedImage.getHeight(); j1++) {
            for (int j2 = bufferedImage.getMinX(); j2 < bufferedImage.getWidth(); j2++) {
                int rgb = bufferedImage.getRGB(j2, j1);
                int R = (rgb & 0xff0000) >> 16;
                int G = (rgb & 0xff00) >> 8;
                int B = (rgb & 0xff);
                if (((255 - R) < 30) && ((255 - G) < 30) && ((255 - B) < 30)) {
                    rgb = ((alpha + 1) << 24) | (rgb & 0x00ffffff);
                }
                bufferedImage.setRGB(j2, j1, rgb);
            }
        }
        g2D.drawImage(bufferedImage, 0, 0, imageIcon.getImageObserver());
        // 输出文件
        ImageIO.write(bufferedImage, "png", outputFile);
    }
}
