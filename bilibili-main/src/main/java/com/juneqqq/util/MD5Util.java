package com.juneqqq.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.MessageDigest;

/**
 * MD5加密
 * 单向加密算法
 * 特点：加密速度快，不需要秘钥，但是安全性不高，需要搭配随机盐值使用
 *
 */
public class MD5Util {

	public static String sign(String content, String salt, String charset) {
		content = content + salt;
		return DigestUtils.md5Hex(getContentBytes(content, charset));
	}

	public static boolean verify(String content, String sign, String salt, String charset) {
		content = content + salt;
		String mysign = DigestUtils.md5Hex(getContentBytes(content, charset));
		return mysign.equals(sign);
	}

	private static byte[] getContentBytes(String content, String charset) {
		if (!"".equals(charset)) {
			try {
				return content.getBytes(charset);
			} catch (UnsupportedEncodingException var3) {
				throw new RuntimeException("MD5签名过程中出现错误,指定的编码集错误");
			}
		} else {
			return content.getBytes();
		}
	}

	//获取文件md5加密后的字符串
    public static String getFileMD5(MultipartFile file) throws Exception {
		InputStream fis = file.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int byteRead;
		while((byteRead = fis.read(buffer)) > 0){
			baos.write(buffer, 0, byteRead);
		}
		fis.close();
		return DigestUtils.md5Hex(baos.toByteArray());
    }


	/**
	 * 计算文件hash
	 */
	public static String hashFile(File file) throws Exception {
		FileInputStream fis = null;
		String sha256 = null;
		try {
			fis = new FileInputStream(file);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = fis.read(buffer, 0, 1024)) != -1) {
				md.update(buffer, 0, length);
			}
			byte[] digest = md.digest();
			sha256 = byte2hexLower(digest);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("计算文件hash值错误");
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sha256;
	}

	private static String byte2hexLower(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int i = 0; i < b.length; i++) {
			stmp = Integer.toHexString(b[i] & 0XFF);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs;
	}

	}