# serio-media
Process media files

主要用于处理视频、 音频、 图片等一些媒体资源，代码基于`sauronsoftware`开源项目进行优化和升级。
[点击查看文档](http://www.sauronsoftware.it/projects/jave/manual.php)

##### MediaDemo

```JAVA

	// 视频截图
	public void testCut() {
		
		File source = new File("C:\\Users\\serio.shi\\Videos\\3zhzI640.mp4");
		File target = new File("C:\\Users\\serio.shi\\Videos\\3zhzI64ssss17.jpg");
		try {
			VideoProcessor videoProcessor = new VideoProcessor();
			videoProcessor.videoCapture(source, target, 340f, 640, 360);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
	}
	
	// 视频转码
	public void testTrasncoder() {
		
		File source = new File("C:\\Users\\serio.shi\\Videos\\3zhzI640.mp4");
		File target = new File("C:\\Users\\serio.shi\\Videos\\3zhzI64ssss17.3gp");
		try {
			VideoProcessor videoProcessor = new VideoProcessor();
			videoProcessor.transcode(source, target);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
	}
	
	public void testVideoInfo() {
		try {
			File source = new File("C:\\Users\\serio.shi\\Videos\\3zhzI640.mp4");
			VideoProcessor videoProcessor = new VideoProcessor();
			videoProcessor.getVideInfo( source );
		} catch (EncoderException e) {
			e.printStackTrace();
		}
	}
	
```