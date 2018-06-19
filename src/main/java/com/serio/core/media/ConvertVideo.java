package com.serio.core.media;
//失败的话把文件移到失败文件目录
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serio.core.utils.ConfigFileParams;
import com.serio.core.utils.Constants;
import com.serio.core.utils.FileConfig;
import com.serio.core.utils.FileUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ConvertVideo {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String videoExtention = ".mp4";	// 转码后视频的扩展名
	public static final String sdVideoSuffix = "sd" + videoExtention;	// 标清后缀名
	public static final String hdVideoSuffix = "hd" + videoExtention;	// 高清后缀名
	public static final String videoPicExtention = ".jpg";	// 转码后视频图片的扩展名
	
	private int orginalWidth = 0;
	private int orginalHeight = 0;
	private String sourceVideoInfo = "";
	private boolean needConvert = true;      

	private String srcVideoFile; 		// 源文件
	private String distVideoFileSd; 	// 目标文件（标清）
	private String distVideoFileHd;	// 目标文件（高清）
	private String imgFile;			// 图片文件
	private String sdFailFile;			// 标清转码失败的文件
	private String hdFailFile;			// 高清转码失败的文件
	private String sdBitRate;			// 标清码率
	private String sdWidth;			// 标清宽度
	private String sdHeight;			// 标清高度
	private String hdBitRate;			// 高清码率
	private String hdWidth;			// 高清宽度
	private String hdHeight;			// 高清高度
	private String uploadDate = "";	// 上传的月份
	private double totaltime=0;         //源文件的总时间
	
	private String ffmpeg = System.getProperty("os.name").startsWith("Windows")? "ffmpeg.exe":"ffmpeg";			
	private String ffprobe = System.getProperty("os.name").startsWith("Windows")? "ffprobe.exe":"ffprobe";			
	
	public ConvertVideo(){
	}
	public ConvertVideo(String srcVideoFile, String imgFile, String distVideoFileSd,
			String distVideoFileHd) {
		InitParameters(srcVideoFile, imgFile, distVideoFileSd, distVideoFileHd, "");
	}

	public ConvertVideo(String srcVideoFile, String imgFile, String distVideoFileSd,
			String distVideoFileHd, String uploadDate) {
		InitParameters(srcVideoFile, imgFile, distVideoFileSd, distVideoFileHd, uploadDate);
	}
	
	public void InitParameters(String srcVideoFile, String imgFile, String distVideoFileSd,
			String distVideoFileHd, String uploadDate) {
		this.uploadDate = uploadDate;
		this.srcVideoFile = FileConfig.getSuccessDir(Constants.FolderType.UPLOAD) + srcVideoFile; 		// 源文件
		
		this.distVideoFileSd = FileConfig.getTranscodingSuccessDir() + uploadDate + distVideoFileSd; 	// 目标文件（标清）
		this.sdFailFile = FileConfig.getTranscodingFailureDir() + distVideoFileSd;			// 标清转码失败的文件
		this.sdBitRate = ConfigFileParams.getSdBitRate();		// 标清码率
		this.sdWidth = ConfigFileParams.getSdWidth();			// 标清宽度
		
		this.imgFile = FileConfig.getTranscodingSuccessDir() + uploadDate + imgFile;			// 图片文件
		
		if (StringUtils.isNotBlank(distVideoFileHd)) {
			this.distVideoFileHd = FileConfig.getTranscodingSuccessDir() + uploadDate + distVideoFileHd;	// 目标文件（高清）
			this.hdFailFile = FileConfig.getTranscodingFailureDir() + distVideoFileHd;		// 高清转码失败的文件
			this.hdBitRate = ConfigFileParams.getHdBitRate();		// 高清码率
			this.hdWidth = ConfigFileParams.getHdWidth();			// 高清宽度
		}
	}


	/**
	 * 转码
	 * @return 转码结果，右起第一个四位表示标清转码结果（0000表示失败；0001表示成功）
	 * 		右起第二个四位表示高清转码结果（0000表示失败；0001表示成功）
	 */
	public int convert() {
		logger.info(srcVideoFile + "开始转码...");
		int result = 0x00;
		if (!FileUtils.isFile(srcVideoFile)) {
			logger.warn(srcVideoFile + "不是有效文件");
			return result;
		}
		
		long startTime = System.currentTimeMillis();

		// 创建需要的目录
		File monthDir = new File(FileConfig.getTranscodingSuccessDir() + uploadDate); 	// 目标文件（标清）
		if (!monthDir.exists()) {
			monthDir.mkdirs();
		}
		
		result = process();
		long endTime = System.currentTimeMillis();
		if ((result & 0x01) == 0x01) {	// 标清成功，进行截图
			capturePic(distVideoFileSd, imgFile);
			logger.info(srcVideoFile + "转码完成，耗时" + (endTime-startTime) + "ms.");
		} else if (result == 0x00){
			logger.warn(srcVideoFile + "转码失败...");
		}
		
		return result;
	}
	
	/**
	 * 根据视频类型分别以ffmpeg和mencoder进行转码
	 * @return 转码结果，右起第一个四位表示标清转码结果（0000表示失败；0001表示成功）
	 * 		右起第二个四位表示高清转码结果（0000表示失败；0001表示成功）
	 */
	protected int process() {
		int type = checkContentType();
		int result = 0x00;	// 转码结果
		boolean status = false;
		//2013/12/19 lixiaoqing 使用最新ffmpeg转码,不再需要mencoder辅助.
		if (type == 0 || type == 2) {	// 直接用ffmpeg将源文件转成目标文件
			
			// 获取视频的原始宽度和高度
			int r = initOriginalWH(srcVideoFile);
			if (r != 1) {
				logger.warn(srcVideoFile + "原始宽度和高度解析不成功...");
				return result;
			} else {
				this.sdHeight = getHeight(sdWidth);				// 标清高度
				if (StringUtils.isNotBlank(distVideoFileHd)) {
				  this.hdHeight = getHeight(hdWidth);				// 高清高度
				}
			}
			
			boolean isMkv = (type == 2);
			
			// 标清转码
			status = ffmpegConvertToMp4(srcVideoFile, distVideoFileSd, sdBitRate, sdWidth + "*" + sdHeight, isMkv);
			if (status) {
				result = result | 0x01;	// 成功标志
			} else {	// 失败的话把文件移到失败文件目录
				result = result & 0x10;	// 失败标志
	    	File file = new File(distVideoFileSd);
	    	if (file.exists())
	    		FileUtils.moveFile(file, new File(sdFailFile));
			} 
			// 高清转码
			if (StringUtils.isNotBlank(distVideoFileHd)) {
				if(!needConvert) {
					try {
						FileUtil.copyFile(new File(srcVideoFile), new File(distVideoFileHd));
						status = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						status = false;
					}
				} else {
					status = ffmpegConvertToMp4(srcVideoFile, distVideoFileHd, hdBitRate, hdWidth + "*" + hdHeight, isMkv);
				}
				
				if (status) {
					result = result | 0x10;	// 成功标志
				} else {	// 失败的话把文件移到失败文件目录
					result = result & 0x01;	// 失败标志
					File file = new File(distVideoFileHd);
					if (file.exists())
						FileUtils.moveFile(file, new File(hdFailFile));
				} 
			}
		}
		return result;
	}

	/**
	 * 判断视频类型
	 * @param path
	 * @return 视频类型：0-格式为：asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv。
	 * 					1-格式为：wmv9，rm，rmvb。
	 * 					9-其他格式
	 */
	protected int checkContentType() {
		String suffixName = FileUtils.getExtention(srcVideoFile);
     //2013/12/19 lixiaoqing：最新ffmpeg可支持rm、rmvb等视频格式，不在需要转为avi中间格式。
		if (suffixName.equals(".avi")) {
			return 0;
		} else if (suffixName.equals(".mpg")) {
			return 0;
		} else if (suffixName.equals(".wmv")) {
			return 0;
		} else if (suffixName.equals(".3gp")) {
			return 0;
		} else if (suffixName.equals(".mov")) {
			return 0;
		} else if (suffixName.equals(".mp4")) {
			return 0;
		} else if (suffixName.equals(".asf")) {
			return 0;
		} else if (suffixName.equals(".asx")) {
			return 0;
		} else if (suffixName.equals(".flv")) {
			return 0;
		} else if (suffixName.equals(".wmv9")) {
			return 0;
		} else if (suffixName.equals(".rm")) {
			return 0;
		} else if (suffixName.equals(".rmvb")) {
			return 0;
		} else if(suffixName.equals(".mkv")) {
			return 2;
		} else if(suffixName.equals(".vob")) {
			return 0;
		}
		return 9;
	}
	
	private boolean IsNeedConvertMP4() {
	  return true;
	}
	
	private boolean CheckVideoInfo()
	{
		JSONObject jsonObject = JSONObject.fromObject(sourceVideoInfo);
		JSONArray streamArray = jsonObject.getJSONArray("streams");
		for(int i=0; i< streamArray.size();i++) {
			JSONObject jObj = streamArray.getJSONObject(i);
			if(jObj.has("width")) {
				orginalWidth  =  jObj.getInt("width");
				orginalHeight = jObj.getInt("height");
				logger.info("video width is : " + orginalWidth + " Height is : "+ orginalHeight);
				return true;
			}
		}
		logger.error("ERROR: NOT FOUND WIDTH AND HEIGHT!!!");
		return false;
	}

	/**
	 * 格式为：asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv,rm,rmvb,wmv9,vob的转码
	 * 为MP4格式
	 * 
	 * @param srcVideoFile 转码文件
	 * @param distVideoFile 转码后文件
	 * @param codingType 转码类型：0x01表示标清；0x10表示高清
	 * @return
	 */
	protected boolean ffmpegConvertToMp4(String srcVideoFile, String distVideoFile, String bitRate, String size, boolean isMKV) {
		long startTime = System.currentTimeMillis();
		List<String> commend = new ArrayList<String>();
		commend.add(FileConfig.getConvertToolsDir() + ffmpeg);
		commend.add("-i"); 
		commend.add(srcVideoFile); 
		if (isMKV) {
			commend.add("-map"); 
			commend.add("0:0");
			commend.add("-map"); 
			commend.add("0:1"); 	
		}
		// 视频选项
		commend.add("-f");
		commend.add("mp4");	 // 输出mp4格式
		commend.add("-vcodec");	// 编码器
		commend.add("libx264");
		commend.add("-b:v");	// 视频码率
		commend.add(bitRate);
		commend.add("-b:a");	// 音频码率
		//test jenkins updata version to 519 Why not updata chen
		commend.add("32k");
		commend.add("-s");	// 图像分辨率
		commend.add(size);
				
		commend.add("-y"); 	// 覆盖输出文件
		commend.add(distVideoFile);
		
		//------------------源视频总时间的获取开始-----------------
//		totaltime=getTotaltime(srcVideoFile);
		//------------------源视频总时间的获取开始----------------	-
		logger.info(srcVideoFile + " -> " + distVideoFile + "转码命令: " + commend.toString());

		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			Process p = builder.start();
			doWaitFor(p);
			p.destroy();
			long endTime = System.currentTimeMillis();
			logger.info(srcVideoFile + " -> " + distVideoFile + " 转码成功,耗时 : (ms)" + (endTime-startTime));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void capturePicTestModel(String videoFile, String imgFile) {
	    srcVideoFile=videoFile;
	    initOriginalWH(srcVideoFile);
	    capturePic(videoFile,imgFile);
	}
	
	/**
	 * 图片截取
	 *
	 * @param videoFile 要截图的文件
	 * @param imgFile 截后的图片名
	 * @param imgWidth 图片宽度
	 * @param imgHeight 图片高度
	 */
	public boolean capturePic(String videoFile, String imgFile) {
		List<String> commend = new ArrayList<String>();
		commend.add(FileConfig.getConvertToolsDir() + ffmpeg);
		commend.add("-ss");
		
		// format HH:MM:SS.XX
//		String[] dur = getTotaltime("Duration: ").split(":|\\.");
		String pos = "00:00:";
//		if (!(dur[0].equals("00") && dur[1].equals("00")))
//			pos += "30";
//		else if (dur[2].compareTo("05") > 0)
//			pos += "05";
//		else
//			pos += "00";
		long dur = 0;
		JSONObject jsonObject = JSONObject.fromObject(sourceVideoInfo);
		JSONArray streamArray = jsonObject.getJSONArray("streams");
		for(int i=0; i< streamArray.size();i++) {
			JSONObject jObj = streamArray.getJSONObject(i);
			if(jObj.has("duration")) {
				dur  =  jObj.getInt("duration");
				break;
			}
		}
		if(dur>30.0)
			pos += "30";
		else if(dur>5.0)
			pos += "05";
		else
			pos += "00";
		commend.add(pos); 	// 图片在视频中的秒数
		commend.add("-i"); 
	    commend.add(videoFile); 
	    commend.add("-f"); 
	    commend.add("image2"); 
	    commend.add("-s"); 
	    commend.add("640*360");     // 16:9, should be fixed value, else can't be adjusted in vpm
//	    commend.add(imgWidth+"*"+imgHeight); 
	    commend.add("-y"); 
	    commend.add(imgFile); 

		logger.info(srcVideoFile + " -> " + imgFile + " 截图命令 : " + commend.toString());
		
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			Process p = builder.start();
			doWaitFor(p);
			p.destroy();
			logger.info(srcVideoFile + " -> " + imgFile + " 截图成功");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
  protected int doWaitFor(Process p) {
    int exitValue = -1; // returned to caller when p is finished
    try {
      InputStream in = p.getInputStream();
      InputStream err = p.getErrorStream();
      boolean finished = false; // Set to true when p is finished

      while (!finished) {
        try {
          while (in.available() > 0) {
            // Print the output of our system call
            Character c = new Character((char) in.read());
            System.out.print(c);
          }
          while (err.available() > 0) {
            // Print the output of our system call
            Character c = new Character((char) err.read());
            System.out.print(c);
          }

          // Ask the process for its exitValue. If the process
          // is not finished an IllegalThreadStateException
          // is thrown. If it is finished we fall through and
          // the variable finished is set to true.
          exitValue = p.exitValue();
          finished = true;

        } catch (IllegalThreadStateException e) {
          // Process is not finished yet;
          // Sleep a little to save on CPU cycles
          //e.printStackTrace();
          TimeUnit.MILLISECONDS.sleep(500L);
        }
      }
    } catch (Exception e) {
      // unexpected exception! print it out for debugging...
      e.printStackTrace();
    }
    return exitValue;
  }
	
	/**
	 * 得到视频原始的宽高
	 * @param original
	 * @return 1表示正常；0表示正常终止；-1表示非正常结束
	 */
	protected int initOriginalWH(String originalFile) {
		StringBuilder videoInfo = new StringBuilder();
		//ffprobe -v quiet -print_format json -show_streams somefile.mov
		List<String> commend = new ArrayList<String>();   
		commend.add(FileConfig.getConvertToolsDir() + ffprobe);   
		commend.add("-v");
		commend.add("quiet");
		commend.add("-print_format");
		commend.add("json");
		commend.add("-show_streams");
		commend.add("-i");   
		commend.add(originalFile);

		logger.info("video basic format info probe commend" + commend.toString());
		int result = -1;      
		try {   
			ProcessBuilder builder = new ProcessBuilder();   
			builder.command(commend);   
			builder.redirectErrorStream(true);   
			Process p = builder.start();   
			BufferedReader buf = null; 
			String line = null;   
			buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = buf.readLine()) != null) {   
				videoInfo.append(line);   
				continue;   
			}   
			result = p.waitFor();
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		this.sourceVideoInfo = videoInfo.toString();
		//logger.info(originalFile + "video basic format info" + this.sourceVideoInfo);
		if (CheckVideoInfo())
			return 1;
		 return -1;
	}
	
	/**
	 * 根据宽度按比例得出高度(16:9,4:3)
	 * @param sdWidth
	 * @return
	 */
	protected String getHeight(String sdWidth) {
		Double sdWidthD = Double.valueOf(sdWidth);
		double h;
		if (orginalHeight*16 == orginalWidth*9)
			h = sdWidthD * 9 / 16;
		else
			h = sdWidthD * 3 / 4;
		
		int hInt = Double.valueOf(h).intValue();
		if (hInt%2 != 0) {	// 不为偶数
			hInt = hInt + 1;
		}
		return String.valueOf(hInt);
	} 
		
	
	// get attribute of source video. for example duration
	protected String getTotaltime(String attrName) {
		int index = sourceVideoInfo.indexOf(attrName);
		if (index != -1) {
			index += attrName.length();
			int endIndex = sourceVideoInfo.indexOf(',', index);
			if (endIndex == -1)
				endIndex = sourceVideoInfo.length();
			return sourceVideoInfo.substring(index, endIndex);
		}
		return "";		
	}

}
