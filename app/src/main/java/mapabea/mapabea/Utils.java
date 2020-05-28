package mapabea.mapabea;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aaa");
	public static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static boolean canDrawOverlays(Context context){
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}else{
			return Settings.canDrawOverlays(context);
		}


	}

	public static String getStringifyCurrentDate() {
		return dateFormat2.format(new Date());
	}

	public static double distance(double lat1, double lat2, double lon1,
								  double lon2) {

//		final int R = 6371; // Radius of the earth
//
//		double latDistance = Math.toRadians(lat2 - lat1);
//		double lonDistance = Math.toRadians(lon2 - lon1);
//		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//		double distance = R * c * 1000; // convert to meters
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        dist = (dist * 1.609344) * 1000;


		return dist;
	}

	public static byte[] bitmapToRaw(Bitmap bitmap) {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		//bitmap.recycle();
		return byteArray;
	}

	public Bitmap fileToBitmap(String path){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}
}
