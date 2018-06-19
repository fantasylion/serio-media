package com.serio.core.media;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.serio.core.annotation.AnnotationProcesser;
import com.serio.core.annotation.media.FfmpegOption;
import com.serio.core.annotation.media.FfmpegOption.OptionType;
import com.serio.core.utils.ReflectionUtils;

/**
 * The ffmpeg options handle
 * @author zl.shi
 */
public class OptionProcesser {

	EncodingAttributes		attributes;
	
	AudioAttributes			audioAttributes;
	
	VideoAttributes			videoAttributes;
	
	Map<String, Object> inputTypemap  = new HashMap<String, Object>();
	Map<String, Object> outputTypeMap = new HashMap<String, Object>();
	Map<String, Object> globalTypeMap = new HashMap<String, Object>();
	
	public OptionProcesser( EncodingAttributes attributes, AudioAttributes audioAttributes, VideoAttributes videoAttributes ) {
		this.attributes      = attributes;
		this.audioAttributes = audioAttributes;
		this.videoAttributes = videoAttributes;
	}
	
	/**
	 * Process the ffmpeg options and set the options to ffmpeg.
	 * @author zl.shi
	 * @param locator
	 * @param source
	 * @param target
	 * @return
	 */
	public FFMPEGExecutor process( FFMPEGLocator locator,  File source, File target ) {
		return process( locator.createExecutor(), source, target );
	}
	

	/**
	 * <p>By the Global options, input options output options to classifying the type and set to ffmpeg.</p>
	 * <p>ffmpeg [global_options] {[input_file_options] -i input_url} ... {[output_file_options] output_url} ...</p>
	 * @author zl.shi
	 * @param ffmpeg
	 * @param source
	 * @param target
	 * @return
	 */
	public FFMPEGExecutor process( FFMPEGExecutor ffmpeg, File source, File target ) {
		try {
			Map<Field, Object> mainTypemap = AnnotationProcesser.getAllAnnotation(EncodingAttributes.class, FfmpegOption.class);
			Map<Field, Object> videoTypemap = AnnotationProcesser.getAllAnnotation(VideoAttributes.class, FfmpegOption.class);
			Map<Field, Object> audioTypemap = AnnotationProcesser.getAllAnnotation(AudioAttributes.class, FfmpegOption.class);
			
			classifyingType( mainTypemap, attributes );
			classifyingType( audioTypemap, audioAttributes );
			classifyingType( videoTypemap, videoAttributes );
			
			processAllAttribute( source, target, ffmpeg );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ffmpeg;
	}
	
	
	/**
	 * Classifying the type for Global options, input options, output options.
	 * @author zl.shi
	 * @param typeMap
	 * @param attribute
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void classifyingType( Map<Field, Object> typeMap, Object attribute ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		if ( attribute == null ) {
			return;
		}
		
		Object arg = null;
		for ( Entry<Field, Object> entry : typeMap.entrySet() ) {
			
			FfmpegOption ffmpegOption = (FfmpegOption)entry.getValue();
			if ( ffmpegOption.type() == null ) {
				continue;
			}
			
			switch ( ffmpegOption.type() ) {
				case INPUT:
					arg = ReflectionUtils.getObjectAttr(attribute, entry.getKey(), attribute.getClass());
					if ( arg != null )
						inputTypemap.put(ffmpegOption.value(), arg);
					break;
				case OUTPUT:
					arg = ReflectionUtils.getObjectAttr(attribute, entry.getKey(), attribute.getClass());
					if ( arg != null )
						outputTypeMap.put(ffmpegOption.value(), arg);
					break;
				case GLOBAL:
					arg = ReflectionUtils.getObjectAttr(attribute, entry.getKey(), attribute.getClass());
					if ( arg != null )
						globalTypeMap.put(ffmpegOption.value(), arg);
					break;
				default:
					break;
			}
		}
	}
	
	
	/**
	 * Set all of the Global options, input options, output options to ffmpeg.
	 * @author zl.shi
	 * @param source
	 * @param target
	 * @param ffmpeg
	 */
	public void processAllAttribute( File source, File target, FFMPEGExecutor ffmpeg ) {
		processAttribute( ffmpeg, globalTypeMap );
		ffmpeg.addArgument("-y");
		processAttribute( ffmpeg, inputTypemap );
		ffmpeg.addArgument("-i");
		ffmpeg.addArgument(source.getAbsolutePath());
		processAttribute( ffmpeg, outputTypeMap );
		ffmpeg.addArgument(target.getAbsolutePath());
	}
	
	
	/**
	 * Set the Global options, input options, output options to ffmpeg.
	 * @author zl.shi
	 * @param ffmpeg
	 * @param typeMap
	 */
	public void processAttribute( FFMPEGExecutor ffmpeg, Map<String, Object> typeMap ) {
		for ( Entry<String, Object> entry : typeMap.entrySet() ) {
			ffmpeg.addArgument(entry.getKey());
			ffmpeg.addArgument(String.valueOf(entry.getValue()));
		}
		
	}
	
	
	/**
	 * Set the Global options, input options, output options to ffmpeg one by one.
	 * @author zl.shi
	 * @param ffmpeg
	 * @param typeMap
	 */
	public void process( FFMPEGExecutor ffmpeg, Map<Field, Object> typeMap ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for ( Entry<Field, Object> entry : typeMap.entrySet() ) {
			FfmpegOption ffmpegOption = (FfmpegOption)entry.getValue();
			if ( ffmpegOption.value() == null ) {
				continue;
			}
			
			Field field = (Field)entry.getKey();
			Object arg = ReflectionUtils.getObjectAttr(audioAttributes, field, audioAttributes.getClass());
			if ( arg == null ) {
				continue;
			}
			
			ffmpeg.addArgument(String.valueOf(ffmpegOption.value()));
			ffmpeg.addArgument(String.valueOf(arg));
			
		}
	}
	
	
}
