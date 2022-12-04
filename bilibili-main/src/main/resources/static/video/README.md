#### 介绍
一款用于在网页上播放视频的软件，本软件为开源软件，遵守开源协议：MIT

#### 视频格式支持
支持mp4,flv,m3u8,ts等格式

#### 播放形式支持
支持点播、直播、直播+回放

#### 平台支持
支持移动端，PC端。

#### 功能支持
支持使用js监听播放器的各种状态，支持使用js控制播放器的各种操作

#### 软件架构
本软件采用javascript编写核心代码，css文件定义播放器风格

#### 安装教程
不存在安装过程，将下载包里的ckplayer文件夹上传到网站环境中，在需要播放视频的页面上引入ckplayer.js及ckplayer.css文件，在需要插入视频的地方使用如下代码：

```
<link type="text/css" rel="stylesheet" href="ckplayer/css/ckplayer.css" />
<script type="text/javascript" src="ckplayer/js/ckplayer.js" charset="UTF-8"></script>
<div class="video" style="width: 600px;height: 400px;">播放器容器</div>
<script type="text/javascript">
    //定义一个变量：videoObject，用来做为视频初始化配置
    var videoObject = {
        container: '.video', //“#”代表容器的ID，“.”或“”代表容器的class
        video: 'http://ckplayer-video.oss-cn-shanghai.aliyuncs.com/sample-mp4/05cacb4e02f9d9e.mp4'//视频地址
    };
    var player = new ckplayer(videoObject);//初始化播放器
</script>
```


#### 使用说明
使用过程中碰到问题，请至官网查看手册：http://www.ckplayer.com/manual/

#### 参与贡献
niandeng

