package com.serio.core.media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serio.core.utils.FileConfig;

public class ConvertTool{
  protected static Logger logger = LoggerFactory.getLogger("ConvertTool");
  
  /**
   * return false, if destFilePath exists or srcFilePath doesn't exist
   * @param: srcFilePath, absolute path of source file
   * @param: destFilePath, absolute path
   */
    public static boolean IsConvertable(String srcFilePath, String destFilePath) {
      if (new File(destFilePath).isFile()) {
        logger.warn("File {} is exist", destFilePath);
        return false;
      }
      
      if (!new File(srcFilePath).isFile()) {
        logger.warn("File {} doesn't exist", srcFilePath);
        return false;
      }
      return true;
    }
  
    /**
     * @param: srcVideoPath, absolute path of source file
     * @param: destVideoFile, absolute path
     */
    public static List<String> getConvertTSCommand(String srcVideoPath, String destVideoFile) {
      if (IsConvertable(srcVideoPath, destVideoFile)) {
        String ffmpegName = System.getProperty("os.name").contains("Windows")? "ffmpeg.exe":"ffmpeg";
        List<String> command = new ArrayList<String>();
        command.add(FileConfig.getConvertToolsDir() + ffmpegName);
        command.add("-i"); 
        command.add(srcVideoPath);
        command.add("-vcodec");
        command.add("copy");    //libx264
        command.add("-acodec");
        command.add("copy");
        command.add("-vbsf");
        command.add("h264_mp4toannexb");
        command.add(destVideoFile);
        return command;
      }
      return null;
    }
        
    /**
     * video rate command list
     * @param: srcVideoPath, absolute path of source file
     * @param: bitRate, for example, 256k
     * @param: size, for example, 320*240
     * @param: destVideoFile, absolute path
     */
    public static List<String> getConvertRateCommand(String srcVideoPath, String bitRate, String size, String destVideoFile) {
      if (IsConvertable(srcVideoPath, destVideoFile)) {
        String ffmpegName = System.getProperty("os.name").contains("Windows")? "ffmpeg.exe":"ffmpeg";
        List<String> command = new ArrayList<String>();
        command.add(FileConfig.getConvertToolsDir() + ffmpegName);
        command.add("-i"); 
        command.add(srcVideoPath); 
        // 视频选项
        command.add("-f");
        command.add("mp4");  // 输出mp4格式
        command.add("-vcodec"); // 编码器
        command.add("libx264");
        command.add("-b:v");    // 视频码率
        command.add(bitRate);
        command.add("-b:a");    // 音频码率
        command.add("32k");
        command.add("-s");  // 图像分辨率
        command.add(size);
                
        command.add("-y");  // 覆盖输出文件
        command.add(destVideoFile);
        
        return command;
      } else {
        return null;
      }
    }
    
    /**
     * video rate command list
     * @param: srcVideoPath, absolute path of source file
     * @param: size, for example, 320*240
     * @param: distImageFile, absolute path
     */
    public static List<String> getCaptureImageCommand(String srcVideoPath, String size, String distImageFile) {
      if (IsConvertable(srcVideoPath, distImageFile)) {
        String ffmpegName = System.getProperty("os.name").contains("Windows")? "ffmpeg.exe":"ffmpeg";
        List<String> command = new ArrayList<String>();
        command.add(FileConfig.getConvertToolsDir() + ffmpegName);
        command.add("-ss");
        command.add("00:00:01");   // 图片在视频中的秒数
        command.add("-i"); 
        command.add(srcVideoPath); 
        command.add("-f"); 
        command.add("image2"); 
        command.add("-s"); 
        command.add(size);
        command.add("-y"); 
        command.add(distImageFile);
        
        return command;
      } else {
        return null;
      }
    }
}
