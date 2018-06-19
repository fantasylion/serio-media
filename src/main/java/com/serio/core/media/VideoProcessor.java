package com.serio.core.media;

import java.io.File;
import java.io.IOException;

/**
 * @author zl.shi
 */
public class VideoProcessor {
	
	/**
	 * The locator of the ffmpeg executable used by this encoder.
	 */
	private FFMPEGLocator locator;

	/**
	 * It builds an encoder using a {@link DefaultFFMPEGLocator} instance to
	 * locate the ffmpeg executable to use.
	 */
	public VideoProcessor() {
		this.locator = new DefaultFFMPEGLocator();
	}

	/**
	 * It builds an encoder with a custom {@link FFMPEGLocator}.
	 * 
	 * @param locator
	 *            The locator picking up the ffmpeg executable used by the
	 *            encoder.
	 */
	public VideoProcessor(FFMPEGLocator locator) {
		this.locator = locator;
	}
	
	
	/**
	 * Cut the video to image by the specify time.
	 * @author zl.shi
	 * @param videoSource
	 * @param imgSource
	 * @param time			like 0.2f
	 * @param sizeWitdh		like 640
	 * @param sizeHeight	like 360
	 * @throws InputFormatException 
	 * @throws IllegalArgumentException 
	 * @throws EncoderException
	 */
	public void videoCapture( File videoSource, File imgSource, float time, Integer sizeWitdh, Integer sizeHeight ) throws IllegalArgumentException, InputFormatException, EncoderException {
		
		VideoAttributes video = new VideoAttributes();
		video.setCodec(MediaConstant.PARAMETER_VCODEC_MJPEG);
		video.setFrameRate(1);
		video.setVideoFrames(1);
		video.setSize(new VideoSize(sizeWitdh, sizeHeight));

		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat(MediaConstant.PARAMETER_VCODEC_MJPEG);
		attrs.setOffset(time);
		attrs.setVideoAttributes(video);
		Encoder encoder = new Encoder();
		encoder.encode( videoSource, imgSource, attrs );
		
	}
	
	
	/**
	 * 转码
	 * <p>
	 * eg:<br>
	 * 	videoSource:"C:\\Users\\serio\\Videos\\3zhzI640.mp4"
	 *  destSource:"C:\\Users\\serio\\Videos\\3zhzI640.3gp
	 * </p>
	 * @author zl.shi
	 * @param srcVideoPath
	 * @param bitRate
	 * @param size
	 * @param destVideoFile
	 * @throws IOException 
	 */
	public void transcode( File videoSource, File destSource ) throws IllegalArgumentException, InputFormatException, EncoderException {
		VideoAttributes video = new VideoAttributes();
		video.setCodec(MediaConstant.PARAMETER_CODEC_COPY);
		
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec(MediaConstant.PARAMETER_CODEC_COPY);
		
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setVideoAttributes(video);
		attrs.setAudioAttributes(audio);
		
		Encoder encoder = new Encoder();
		encoder.encode( videoSource, destSource, attrs );
	}
	

}
