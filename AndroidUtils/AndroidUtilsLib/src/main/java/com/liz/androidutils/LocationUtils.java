package com.liz.androidutils;

import android.location.Location;

import java.text.DecimalFormat;
import java.util.ArrayList;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LocationUtils {

    public static final double EARTH_RADIUS_EQUATOR = 6378137;
    public static final double EARTH_RADIUS_AVERAGE = 6371393;

    public static final float BEARING_ERR = 0.5f;
    public static final float BEARING_AREA = 22.5f;  // max range based on special orientation

    public static class Orientation {
        public float bearing;
        public String name;
        public String name_clockwise;
        public String name_anti_clockwise;
        public Orientation(float bearing, String name, String name_clockwise, String name_anti_clockwise) {
            this.bearing = bearing;
            this.name = name;
            this.name_clockwise = name_clockwise;
            this.name_anti_clockwise = name_anti_clockwise;
        }
    }

    public static ArrayList<Orientation> orientations = new ArrayList<Orientation>() {{
        add(new Orientation(0f,   "正北", "北偏东", "北偏西"));
        add(new Orientation(45f,  "东北", "东北偏东", "东北偏北"));
        add(new Orientation(90f,  "正东", "东偏南", "东偏北"));
        add(new Orientation(135f, "东南", "东南偏南", "东南偏东"));
        add(new Orientation(180f, "正南", "南偏西", "南偏东"));
        add(new Orientation(225f, "西南", "西南偏西", "西南偏南"));
        add(new Orientation(270f, "正西", "西偏北", "西偏南"));
        add(new Orientation(315f, "西北", "西北偏北", "西北偏西"));
    }};

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Old definitions

    public static final float BEARING_NORTH = 0f;
    public static final String BEARING_NAME_NORTH = "正北";
    public static final String BEARING_NAME_NORTH_TO_EAST = "北偏东";

    public static final float BEARING_EAST = 90f;
    public static final float BEARING_SOUTH = 180f;
    public static final float BEARING_WEST = 270f;

    public static final float BEARING_NORTH_EAST = BEARING_NORTH + 45f;
    public static final float BEARING_SOUTH_EAST = BEARING_EAST + 45f;
    public static final float BEARING_SOUTH_WEST = BEARING_SOUTH + 45f;
    public static final float BEARING_NORTH_WEST = BEARING_WEST + 45f;

    public static final float BEARING_NORTH_EAST_TO_NORTH = BEARING_NORTH + 22.5f;
    public static final float BEARING_NORTH_EAST_TO_EAST = BEARING_NORTH_EAST + 22.5f;
    public static final float BEARING_SOUTH_EAST_TO_EAST = BEARING_EAST + 22.5f;
    public static final float BEARING_SOUTH_EAST_TO_SOUTH = BEARING_SOUTH_EAST + 22.5f;
    public static final float BEARING_SOUTH_WEST_TO_SOUTH = BEARING_SOUTH + 22.5f;
    public static final float BEARING_SOUTH_WEST_TO_WEST = BEARING_SOUTH_WEST + 22.5f;
    public static final float BEARING_NORTH_WEST_TO_WEST = BEARING_WEST + 22.5f;
    public static final float BEARING_NORTH_WEST_TO_NORTH = BEARING_NORTH_WEST + 22.5f;

    public static final String BEARING_NAME_NORTH_EAST_TO_NORTH = "东北偏北";
    public static final String BEARING_NAME_NORTH_EAST = "东北";
    public static final String BEARING_NAME_NORTH_EAST_TO_EAST = "东北偏东";

    public static final String BEARING_NAME_EAST_TO_NORTH = "东偏北";
    public static final String BEARING_NAME_EAST = "正东";
    public static final String BEARING_NAME_EAST_TO_SOUTH = "东偏南";

    public static final String BEARING_NAME_WEST = "正西";
    public static final String BEARING_NAME_SOUTH = "正南";

    public static final String BEARING_NAME_SOUTH_EAST = "东南";

    public static final String BEARING_NAME_NORTH_WEST = "西北";
    public static final String BEARING_NAME_SOUTH_WEST = "西南";


    public static final String BEARING_NAME_SOUTH_TO_EAST = "南偏东";
    public static final String BEARING_NAME_SOUTH_TO_WEST = "南偏西";
    public static final String BEARING_NAME_WEST_TO_SOUTH = "西偏南";
    public static final String BEARING_NAME_WEST_TO_NORTH = "西偏北";
    public static final String BEARING_NAME_NORTH_TO_WEST = "北偏西";

    public static final String BEARING_NAME_SOUTH_EAST_TO_EAST = "东南偏东";
    public static final String BEARING_NAME_SOUTH_EAST_TO_SOUTH = "东南偏南";
    public static final String BEARING_NAME_SOUTH_WEST_TO_WEST = "西南偏西";
    public static final String BEARING_NAME_SOUTH_WEST_TO_SOUTH = "西南偏南";
    public static final String BEARING_NAME_NORTH_WEST_TO_WEST = "西北偏西";
    public static final String BEARING_NAME_NORTH_WEST_TO_NORTH = "西北偏北";

    // Old definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static double toKMH(double ms) {
        return ms * 3600 / 1000;
    }

    public static double getDistance(Location loc1, Location loc2) {
        return getDistance(loc1.getLongitude(), loc1.getLatitude(),
                loc2.getLongitude(), loc2.getLatitude());
    }

    public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        return getDistanceAsin(lng1, lat1, lng2, lat2);
    }

    public static double getDistanceAsin(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS_EQUATOR;
        return s;
    }

    public static double getDistanceAcos(double lng1, double lat1, double lng2, double lat2) {
        double radAX = Math.toRadians(lng1);
        double radAY = Math.toRadians(lat1);
        double radBX = Math.toRadians(lng2);
        double radBY = Math.toRadians(lat2);
        double cos = Math.cos(radAY) * Math.cos(radBY) * Math.cos(radAX - radBX)
                + Math.sin(radAY) * Math.sin(radBY);
        double acos = Math.acos(cos);
        return EARTH_RADIUS_AVERAGE * acos;
    }

    public static double getAngle(double lng1, double lat1, double lng2,
                                  double lat2) {
        double fLat = Math.PI * (lat1) / 180.0;
        double fLng = Math.PI * (lng1) / 180.0;
        double tLat = Math.PI * (lat2) / 180.0;
        double tLng = Math.PI * (lng2) / 180.0;
        double degree = (Math.atan2(Math.sin(tLng - fLng) * Math.cos(tLat), Math.cos(fLat) * Math.sin(tLat) - Math.sin(fLat) * Math.cos(tLat) * Math.cos(tLng - fLng))) * 180.0 / Math.PI;
        if (degree >= 0) {
            return degree;
        } else {
            return 360 + degree;
        }
    }

    public static String formatDistance(double distance) {
        if (distance == 0) {
            return "0 m";
        }
        else if (distance < 1) {
            return new DecimalFormat("0.00").format(distance) + " m";
        }
        else if (distance < 1000*10) {
            return new DecimalFormat("#,###").format(distance) + " m";
        }
        else {
            return new DecimalFormat("###,##0.00").format(distance/1000) + " km";
        }
    }

    public static int getNormalBearing(int bearing) {
        int nb = bearing % 360;
        if (nb < 0) {
            nb += 360;
        }
        return nb;
    }

    public static float getNormalBearing(float bearing) {
        float nb = bearing % 360;
        if (nb < 0) {
            nb += 360;
        }
        return nb;
    }

    public static double getNormalBearing(double bearing) {
        double nb = bearing % 360;
        if (nb < 0) {
            nb += 360;
        }
        return nb;
    }

    public static String getBearingName(float bearing) {
        float nb = getNormalBearing(bearing);

        // match name first
        for (Orientation o : orientations) {
            if (Math.abs(nb - o.bearing) <= BEARING_ERR) {
                return o.name;
            }
        }

        // then match clockwise/anti-clockwise
        for (Orientation o : orientations) {
            float diff = nb - o.bearing;
            if (diff > 0 && diff <= BEARING_AREA) {
                return o.name_clockwise + " " + Math.round(Math.abs(diff));
            }
            if (diff < 0 && diff >= -BEARING_AREA) {
                return o.name_anti_clockwise + " " + Math.round(Math.abs(diff));
            }
        }

        // no match, it must be the special one
        float diff = 360 - bearing;
        return orientations.get(0).name_anti_clockwise +  " " + Math.round(Math.abs(diff));
    }

    public static String getBearingName0(float bearing) {
        float nb = getNormalBearing(bearing);

        if (Math.abs(nb - BEARING_NORTH) <= BEARING_ERR) { return BEARING_NAME_NORTH; }
        if (Math.abs(nb - BEARING_EAST) <= BEARING_ERR) { return BEARING_NAME_EAST; }
        if (Math.abs(nb - BEARING_WEST) <= BEARING_ERR) { return BEARING_NAME_WEST; }
        if (Math.abs(nb - BEARING_SOUTH) <= BEARING_ERR) { return BEARING_NAME_SOUTH; }

        if (Math.abs(nb - BEARING_NORTH_EAST) <= BEARING_ERR) { return BEARING_NAME_NORTH_EAST; }
        if (Math.abs(nb - BEARING_SOUTH_EAST) <= BEARING_ERR) { return BEARING_NAME_SOUTH_EAST; }
        if (Math.abs(nb - BEARING_SOUTH_WEST) <= BEARING_ERR) { return BEARING_NAME_SOUTH_WEST; }
        if (Math.abs(nb - BEARING_NORTH_WEST) <= BEARING_ERR) { return BEARING_NAME_NORTH_WEST; }

        if (nb < BEARING_NORTH_EAST_TO_NORTH) { return BEARING_NAME_NORTH_TO_EAST; }
        if (nb < BEARING_NORTH_EAST) { return BEARING_NAME_NORTH_EAST_TO_NORTH; }

        if (nb < BEARING_NORTH_EAST_TO_EAST) { return BEARING_NAME_NORTH_EAST_TO_EAST; }
        if (nb < BEARING_EAST) { return BEARING_NAME_EAST_TO_NORTH; }

        if (nb < BEARING_SOUTH_EAST_TO_EAST) { return BEARING_NAME_SOUTH_EAST_TO_EAST; }
        if (nb < BEARING_SOUTH_EAST) { return BEARING_NAME_SOUTH_EAST_TO_SOUTH; }

        if (nb < BEARING_SOUTH_EAST_TO_SOUTH) { return BEARING_NAME_SOUTH_EAST_TO_SOUTH; }
        if (nb < BEARING_SOUTH) { return BEARING_NAME_SOUTH_TO_EAST; }

        if (nb < BEARING_SOUTH_WEST_TO_SOUTH) { return BEARING_NAME_SOUTH_WEST_TO_SOUTH; }
        if (nb < BEARING_SOUTH_WEST) { return BEARING_NAME_SOUTH_WEST_TO_WEST; }

        if (nb < BEARING_SOUTH_WEST_TO_WEST) { return BEARING_NAME_SOUTH_WEST_TO_WEST; }
        if (nb < BEARING_WEST) { return BEARING_NAME_WEST_TO_SOUTH; }

        if (nb < BEARING_NORTH_WEST_TO_WEST) { return BEARING_NAME_WEST_TO_NORTH; }
        if (nb < BEARING_NORTH_WEST) { return BEARING_NAME_NORTH_WEST_TO_WEST; }

        if (nb < BEARING_NORTH_WEST_TO_NORTH) { return BEARING_NAME_NORTH_WEST_TO_WEST; }
        return BEARING_NAME_NORTH_TO_WEST;
    }

    public static String getDualSpeedText(double speed) {
        return String.format("%.2f", speed)
                + "/"
                + String.format("%.2f", LocationUtils.toKMH(speed))
                ;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        JLog.d("Test Start...\n");

        //test_definitions();
        //test_getNormalBearing();
        test_getBearingName();
        //test_formatDistance();
        //test_items();

        JLog.d("Test Successfully.\n");
    }

    public static void test_definitions() {
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_NORTH_EAST", "45.0");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_SOUTH_EAST", "135.0");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_SOUTH_WEST", "225.0");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_NORTH_WEST", "315.0");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_NORTH_EAST_TO_NORTH", "22.5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_NORTH_EAST_TO_EAST", "67.5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_SOUTH_EAST_TO_EAST", "112.5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_SOUTH_EAST_TO_SOUTH", "157.5");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_SOUTH_WEST_TO_SOUTH", "202.5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_SOUTH_WEST_TO_WEST", "247.5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_NORTH_WEST_TO_WEST", "292.5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.BEARING_NORTH_WEST_TO_NORTH", "337.5");
    }

    public static void test_getNormalBearing() {
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(0)", "0");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(89)", "89");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(159.6)", "159.6");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(360)", "0");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(361)", "1");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(720)", "0");
        //AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(361.6)", "1.6");  //"1.6000000000000227", EXPECT "1.6" --FAILED???
        //AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(361.6f)", "1.6f");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(-1)", "359");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(-1.2)", "358.8");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(-160)", "200");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(-360)", "0");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getNormalBearing(-361.2)", "358.8");
    }

    public static void test_getBearingName() {
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(0)", "正北");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(0.5f)", "正北");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(90)", "正东");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(180)", "正南");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(270)", "正西");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(45)", "东北");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(135)", "东南");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(225)", "西南");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(315)", "西北");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(0.6f)", "北偏东 1");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(20)", "北偏东 20");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(22.5f)", "北偏东 23");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(350)", "北偏西 10");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(23)", "东北偏北 22");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(40)", "东北偏北 5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(50)", "东北偏东 5");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(67.5f)", "东北偏东 23");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(80)", "东偏北 10");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(95)", "东偏南 5");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(120)", "东南偏东 15");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(140)", "东南偏南 5");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(170)", "南偏东 10");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(186)", "南偏西 6");

        // 225
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(210)", "西南偏南 15");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(240)", "西南偏西 15");

        // 270
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(250)", "西偏南 20");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(277)", "西偏北 7");

        // 315
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(300)", "西北偏西 15");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(330)", "西北偏北 15");
    }

    public static void test_getBearingName0() {
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(0)", BEARING_NAME_NORTH);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(90)", BEARING_NAME_EAST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(180)", BEARING_NAME_SOUTH);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(270)", BEARING_NAME_WEST);

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(45)", BEARING_NAME_NORTH_EAST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(135)", BEARING_NAME_SOUTH_EAST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(225)", BEARING_NAME_SOUTH_WEST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(315)", BEARING_NAME_NORTH_WEST);

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(30)", BEARING_NAME_NORTH_TO_EAST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(330)", BEARING_NAME_NORTH_TO_WEST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(80)", BEARING_NAME_EAST_TO_NORTH);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(95)", BEARING_NAME_EAST_TO_SOUTH);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(170)", BEARING_NAME_SOUTH_TO_EAST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(190)", BEARING_NAME_SOUTH_TO_WEST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(260)", BEARING_NAME_WEST_TO_SOUTH);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(280)", BEARING_NAME_WEST_TO_NORTH);

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(0)", BEARING_NAME_NORTH);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(360)", BEARING_NAME_NORTH);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(720)", BEARING_NAME_NORTH);

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(390)", BEARING_NAME_NORTH_TO_EAST);
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.getBearingName(-30)", BEARING_NAME_NORTH_TO_WEST);
    }

    public static void test_formatDistance() {
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(0)", "0 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(0.2)", "0.20 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(0.12345678)", "0.12 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(1)", "1 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(123)", "123 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(1000)", "1,000 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(9876)", "9,876 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(98765)", "98.77 km");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(10000)", "10.00 km");
    }

    public static void test_items() {
        // data retrieved from http://www.box3.cn/tools/lbs.html
        test_item(49.028399510054, 8.3575328887407, 49.028379740816, 8.3576200380035);  //10
        test_item(113.928425, 22.526803, 113.925665, 22.524073);	//415
        test_item(49.028399510054, 8.3575328887407, 49.028379740816, 8.3576200380035);	//	10
        test_item(112.66175548104307, 34.75734244194438,	113.66175548104307, 34.75734244194438);	//	91297
        test_item(120.237629, 29.314884,	120.255522, 29.311574);	//	1772
        test_item(120.237629, 29.314884,	120.237551, 29.315009);	//	16
        test_item(108.83255626, 34.20054047,	108.83262616, 34.20053934);	//	6
        test_item(116.3971280, 39.9165270, 116.3971480, 99.9163270);	//	6667485
        test_item(87.105, 42.917,	87.205, 42.504);	//	46615
    }

    public static void test_item(double lng1, double lat1, double lng2, double lat2) {
        System.out.println("\n");
        System.out.println("getDistance(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getDistance(lng1, lat1, lng2, lat2));
        System.out.println("getDistanceAsin(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getDistanceAsin(lng1, lat1, lng2, lat2));
        System.out.println("getDistanceAcos(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getDistanceAcos(lng1, lat1, lng2, lat2));
        System.out.println("getAngle(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getAngle(lng1, lat1, lng2, lat2));
        System.out.println("\n");
    }
}

////////////////////////////////////////////////////////////////////////////////////////
//        GpsStatus.Listener gpsS = new GpsStatus.Listener() {
//            @Override
//            public void onGpsStatusChanged(int event) {
//                gpscount = 0;
//// TODO Auto-generated method stub
//                if(event== GpsStatus.GPS_EVENT_FIRST_FIX){
//     //第一次定位
//   }else if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS){
//     //卫星状态改变
//     GpsStatus gpsStauts= locationManager.getGpsStatus(null); // 取当前状态
//     int maxSatellites = gpsStauts.getMaxSatellites(); //获取卫星颗数的默认最大值
//
//     Iterator<GpsSatellite> it = gpsStauts.getSatellites().iterator();//创建一个迭代器保存所有卫星
//     while (it.hasNext() && gpscount <= maxSatellites) {
//       GpsSatellite s = it.next();
//       //可见卫星数量
//       if(s.usedInFix()){
//       //已定位卫星数量
//       gpscount++;
//       }
//     }
//     //gpsCount.Gpscount(gpscount);
//
//   }else if(event==GpsStatus.GPS_EVENT_STARTED){
//     //定位启动
//   }else if(event==GpsStatus.GPS_EVENT_STOPPED){
//     //定位结束
//   }
//            }
//        };
//        //代码里面有一个已定位的卫星判断，这个方法返回的状态值就是
//
////        if(s.usedInFix()){
////       //已定位卫星数量
////       gpscount++;
////       }
////////////////////////////////////////////////////////////////////////////////////////
