package com.vassarlabs.proj.uniapp.utility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Rotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IDownloadMediaFromS3;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IRotateImageUtility;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IUploadMedia;

import javaxt.io.Image;
@Component
public class RotateImageUtility implements IRotateImageUtility {

	@Autowired private IUploadMedia uploadMediaUtility;
	// for Default bucket
	@Qualifier("DownloadMediaFromS3DefaultBucket")
	//  for specific bucket (production deployment)
//		@Qualifier("DownloadMediaFromS3ProductionBucket")
	@Autowired private IDownloadMediaFromS3 downloadMediaFromS3Utility;
	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Override
	public void rotateImage(FormMediaValue mediaValue) throws IOException {
		if(mediaValue == null) {
			logger.info("mediaValue object is null");
			return;
		}
		Long startTs = System.currentTimeMillis();
		byte[] mediaByteArray = null;
		if(mediaValue.getMediaPath() != null) {
			mediaByteArray = downloadMediaFromS3Utility.downloadFromS3(mediaValue);
		} 
		if(mediaByteArray == null)
			return;
		logger.info("Time Taken to download Image :: " + mediaValue.getMediaPath() + " :: " + (System.currentTimeMillis()-startTs) ) ;

		mediaValue.setMediaContent(mediaByteArray);

//		Image img = new Image(mediaByteArray);
//		boolean isRotated = isRotated(img);
//		if(isRotated) {
//			img.rotate();
//			mediaValue.setMediaContent(img.getByteArray());
//		logger.info("Time Taken to rectify orientaion of Image :: " + mediaValue.getMediaPath() + " :: " + (System.currentTimeMillis()-startTs) );
//		/*Long startTs = System.currentTimeMillis();
		javaxt.io.Image img = new javaxt.io.Image(mediaByteArray);
		Scalr.Rotation orientation = getOrientation(img);;

		if(orientation !=  null)
		{ 
			InputStream in = new ByteArrayInputStream(mediaByteArray);
			BufferedImage bImageFromConvert = ImageIO.read(in);
			BufferedImage rotatedImage = Scalr.rotate(bImageFromConvert, orientation);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(rotatedImage, mediaValue.getMediaFileExtension(), bos );
			mediaValue.setMediaContent(bos.toByteArray()); 
			logger.info("Time Taken to rectify orientaion of Image :: " + mediaValue.getMediaPath() + " :: " + (System.currentTimeMillis()-startTs) );
//			
			try {
				uploadMediaUtility.upload(mediaValue);
				logger.info("Uploaded Image After Rotaion ");
				logger.info("Time Taken to re-upload Image orientaion of Image :: " + mediaValue.getMediaPath() + " :: " + (System.currentTimeMillis()-startTs) ) ;

			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}

		Long endTs = System.currentTimeMillis();
		logger.info("Total time taken to process image :: " + (endTs-startTs));
	}
	@Override
	public boolean isRotated(Image image) {
		if(image == null) {
			logger.info("Image object null");
			return false;
		}
		HashMap<Integer, Object> exif = image.getExifTags();

		if(exif == null || exif.isEmpty()) 
		{ 
			logger.info("No Orientation properties found for image");
			return false;
		}

		boolean flag = false;
		try{
			int orientation = (Integer) exif.get(0x0112);
			String desc = "";
			switch (orientation) {
			case 1: desc = "Top, left side (Horizontal / normal)"; break;
			case 2: desc = "Top, right side (Mirror horizontal)"; break;
			case 3: desc = "Bottom, right side (Rotate 180)"; break;
			case 4: desc = "Bottom, left side (Mirror vertical)"; break;
			case 5: desc = "Left side, top (Mirror horizontal and rotate 270 CW)";
			flag = true;
			break;
			case 6: desc = "Right side, top (Rotate 90 CW)"; 
			flag = true;
			break;
			case 7: desc = "Right side, bottom (Mirror horizontal and rotate 90 CW)";
			flag = true;
			break;
			case 8: desc = "Left side, bottom (Rotate 270 CW)"; 
			flag = true;
			break;
			}
			logger.info("Orientation: " + orientation + " -- " + desc);
		}
		catch(Exception e){
			logger.info("Coudld Not Found  any Orientation properties for image");
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public Rotation getOrientation(Image image) {
		if(image == null) {
			logger.info("Image object null");
			return null	;
		}
		HashMap<Integer, Object> exif = image.getExifTags();
		if(exif == null || exif.isEmpty() || exif.get(0x0112) == null) 
		{ 
			logger.info("No Orientation properties found for image");
			return null;
		}

		Rotation rotation = null;
		try{
			int orientation = (Integer) exif.get(0x0112);
			String desc = "";

			switch (orientation) {
			case 1: desc = "Top, left side (Horizontal / normal)"; break;
			case 2: desc = "Top, right side (Mirror horizontal)"; break;
			case 3: desc = "Bottom, right side (Rotate 180)"; 
					rotation = Scalr.Rotation.CW_180;
			break;
			case 4: desc = "Bottom, left side (Mirror vertical)"; break;
			case 5: desc = "Left side, top (Mirror horizontal and rotate 270 CW)";
			rotation = Scalr.Rotation.CW_270;
			break;
			case 6: desc = "Right side, top (Rotate 90 CW)"; 
			rotation = Scalr.Rotation.CW_90;
			break;
			case 7: desc = "Right side, bottom (Mirror horizontal and rotate 90 CW)";
			rotation = Scalr.Rotation.CW_90;
			break;
			case 8: desc = "Left side, bottom (Rotate 270 CW)"; 
			rotation = Scalr.Rotation.CW_270;
			break;
			}
			logger.info("Orientation: " + orientation + " -- " + desc);
		}
		catch(Exception e){
			logger.info("Could Not Found  any Orientation properties for image");
			e.printStackTrace();
		}
		return rotation;
	}
	
	
	public void rotateImageForPilot(FormMediaValue mediaValue, Integer orientationdegree) throws IOException {
		if(mediaValue == null) {
			logger.info("mediaValue object is null");
			return;
		}
		Long startTs = System.currentTimeMillis();
		byte[] mediaByteArray = null;
		if(mediaValue.getMediaPath() != null) {
			mediaByteArray = downloadMediaFromS3Utility.downloadFromS3(mediaValue);
		} 
		if(mediaByteArray == null)
			return;
		logger.info("Time Taken to download Image :: " + mediaValue.getMediaPath() + " :: " + (System.currentTimeMillis()-startTs) ) ;

		mediaValue.setMediaContent(mediaByteArray);

		javaxt.io.Image img = new javaxt.io.Image(mediaByteArray);
		Scalr.Rotation orientation = null;
		switch (orientationdegree) {
		case 270:
			orientation = Scalr.Rotation.CW_270;
			break;
		case 90:
			orientation = Scalr.Rotation.CW_90;
		default:
			break;
		}
		if(orientation !=  null)
		{ 
			InputStream in = new ByteArrayInputStream(mediaByteArray);
			BufferedImage bImageFromConvert = ImageIO.read(in);
			BufferedImage rotatedImage = Scalr.rotate(bImageFromConvert, orientation);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(rotatedImage, mediaValue.getMediaFileExtension(), bos );
			mediaValue.setMediaContent(bos.toByteArray()); 
			logger.info("Time Taken to rectify orientaion of Image :: " + mediaValue.getMediaPath() + " :: " + (System.currentTimeMillis()-startTs) );
//			
			try {
				uploadMediaUtility.upload(mediaValue);
				logger.info("Uploaded Image After Rotaion ");
				logger.info("Time Taken to re-upload Image orientaion of Image :: " + mediaValue.getMediaPath() + " :: " + (System.currentTimeMillis()-startTs) ) ;

			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}

		Long endTs = System.currentTimeMillis();
		logger.info("Total time taken to process image :: " + (endTs-startTs));
		
	}

}
