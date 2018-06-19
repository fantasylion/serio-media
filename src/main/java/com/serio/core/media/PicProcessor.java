package com.serio.core.media;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serio.core.utils.ConfigFileParams;
import com.serio.core.utils.Constants;
import com.serio.core.utils.FileUtils;

/**
 * @author zl.shi
 */
public class PicProcessor {

	protected static Logger logger = LoggerFactory.getLogger(PicProcessor.class);

	/**
	 * @param sSourceImage
	 *            源文件完整路径
	 * @param sDestImage
	 *            目标文件完整路径
	 * @param sCompression
	 *            压缩比, none, default, fast, max 图片格式转换
	 * @param type
	 */

	public static boolean convertToJPG(String srcImgPath, String destImgPath, String destImgSmall) {

		if (srcImgPath == null || srcImgPath.trim().equals("")) {
			return false;
		}

		if (validate(srcImgPath)) {
			try {
				convert(srcImgPath, destImgPath, Constants.NORMAL_PIC);
			} catch (Exception ex) {
				logger.info("convert exception " + ex.toString());
				return false;
			}
		}
		return true;
	}

	
	public static String convertPicExtention(String srcImgPath) {
		String imgType = FileUtils.getSuffix(srcImgPath);
		if (!IsUsableType(imgType)) {
			imgType = "jpg";
		}
		return imgType;
	}
	

	private static boolean IsUsableType(String imgType) {
		if ("jpg".equalsIgnoreCase(imgType) || "jpeg".equalsIgnoreCase(imgType) || "png".equalsIgnoreCase(imgType)
				|| "gif".equalsIgnoreCase(imgType))
			return true;
		return false;
	}
	

	/**
	 * 线性改变图片尺寸(可同时改变图片格式big.bmp-->small.jpg)
	 * 
	 * @param srcImg
	 *            源Img
	 * @param destPathSmall
	 *            压缩后小图路径
	 * @param width,height
	 *            压缩尺寸
	 * @throws JimiException
	 * @throws IOException
	 */
	private static void convert(String srcPath, String destPathSmall, String type) throws Exception {
		logger.info("enter ConvertPic.convert: " + srcPath);
		File _file = new File(srcPath); // 读入文件
		Image srcImg = javax.imageio.ImageIO.read(_file); // 构造Image对象

		int width = (int) srcImg.getWidth(null); // 得到源图宽
		int height = (int) srcImg.getHeight(null); // 得到源图长

		int iWidth = Integer.parseInt(ConfigFileParams.getPicWidth(type));
		int iHeight = 0;
		try {
			iHeight = Integer.parseInt(ConfigFileParams.getPicHeight(type));
		} catch (Exception e) {
			iHeight = iWidth * height / width;
		}
		logger.info(" width is : " + width + " height is : " + height + " iWidth is : " + iWidth + " iHeight is : "
				+ iHeight);
		String imgType = FileUtils.getSuffix(srcPath);
		if ((width > iWidth || height > iHeight) && !("gif".equalsIgnoreCase(imgType))) {
			// 原图size大于当前所需size，需要改变缩略图宽高
			resize(srcPath, destPathSmall, iWidth, iHeight);
			logger.info("缩略图:" + destPathSmall + ",新宽*高:" + iWidth + "*" + iHeight);
		} else if (!IsUsableType(imgType)) {
			// 原图格式不是jpg/png/gif，转换格式
			resize(srcPath, destPathSmall, width, height);
			logger.info("转换为jpg:" + destPathSmall + ",原格式:" + imgType + ",宽*高:" + width + "*" + width);
		} else {
			FileUtils.copyFile(new File(srcPath), new File(destPathSmall));
			logger.info("拷贝原图,格式:" + imgType + ",宽*高:" + width + "*" + height);
		}

	}

	
	/**
	 * Check the source file
	 * 
	 * @param imgPath
	 * @return
	 */
	private static boolean validate(String imgPath) {
		File tsDestImageBigImageFile = new File(imgPath);
		if (!tsDestImageBigImageFile.exists()) {
			logger.info(" @> JimiImage.convertToJPG() : 要转换的源图像文件路径不存在！");
			return false;
		} else if (!tsDestImageBigImageFile.canRead()) {
			logger.info(" @> JimiImage.convertToJPG() : 要转换的源图像文件路径不可读！");
			return false;
		} else if (!tsDestImageBigImageFile.isFile()) {
			logger.info(" @> JimiImage.convertToJPG() : 要转换的源图像路径不是一个有效的文件名！");
			return false;
		}
		logger.info("validate image ");
		return true;
	}

	
	/**
	 * Resizes an image to a absolute width and height (the image may not be
	 * proportional)
	 * 
	 * @param inputImagePath
	 *            Path of the original image
	 * @param outputImagePath
	 *            Path to save the resized image
	 * @param scaledWidth
	 *            absolute width in pixels
	 * @param scaledHeight
	 *            absolute height in pixels
	 * @throws IOException
	 */
	private static void resize(File inputFile, String outputImagePath, int scaledWidth, int scaledHeight)
			throws IOException {
		// reads input image
		BufferedImage inputImage = ImageIO.read(inputFile);

		// creates output image
		int type = inputImage.getType();
		// if type is 0, BufferedImage will throw an error
		if (type == 0)
			type = BufferedImage.TYPE_INT_RGB;
		BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, type);

		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
		g2d.dispose();

		// extracts extension of output file
		String formatName = FileUtils.getSuffix(outputImagePath);

		// writes to output file
		ImageIO.write(outputImage, formatName, new File(outputImagePath));
	}

	
	/**
	 * Resizes an image to a absolute width and height (the image may not be
	 * proportional)
	 * 
	 * @param inputImagePath
	 *            Path of the original image
	 * @param outputImagePath
	 *            Path to save the resized image
	 * @param scaledWidth
	 *            absolute width in pixels
	 * @param scaledHeight
	 *            absolute height in pixels
	 * @throws IOException
	 */
	private static void resize(String inputImagePath, String outputImagePath, int scaledWidth, int scaledHeight)
			throws IOException {
		// reads input image
		File inputFile = new File(inputImagePath);
		resize(inputFile, outputImagePath, scaledWidth, scaledHeight);
	}
}
