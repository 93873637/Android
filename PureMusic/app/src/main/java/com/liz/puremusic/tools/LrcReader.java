package com.liz.puremusic.tools;

/**
 * LrcReader:
 * Created by liz on 2018/12/23.
 */

/**
 * @author yedaodao
 *    LRC解析类
 */
public class LrcReader{
    private static LrcReader instance=null;
//    private LrcModelList lmlist;
//
//    /**
//     * 得到对象实例
//     */
//    public static LrcReader getInstance()
//    {
//        if(instance==null)
//            instance=new LrcReader();
//        return instance;
//    }
//    /**
//     * 获取LRC文件流
//     * @param iStream
//     * @throws IOException
//     */
//    public void getLrc(InputStream iStream) throws IOException
//    {
//        InputStreamReader iStreamReader=new InputStreamReader(iStream);
//        BufferedReader reader=new BufferedReader(iStreamReader);
//        lmlist=new LrcModelList();
//        String line=null;
//        while((line=reader.readLine())!=null){
//            parseLine(line);
//        }
//    }
//    /**
//     * 逐行解析，将结果存入自定义的LrcModel中
//     * @param line
//     */
//    public void parseLine(String line)
//    {
//        String reg="\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";
//        Pattern pattern= Pattern.compile(reg);
//        Matcher matcher=pattern.matcher(line);
//        while(matcher.find()){
//            String time=matcher.group();
//            LrcModel lModel=new LrcModel();
//            lModel.setCurrentTime(parseTime(time));
//            lModel.setCurrentContent(line.substring(time.length()));
//            lmlist.addEle(lModel);
//        }
//    }
//    /**
//     * 解析时间，转换为毫秒格式
//     * @param time
//     * @return
//     */
//    public Integer parseTime(String time)
//    {
//        String temp=time.substring(1,time.length()-1);
//        String[] s = temp.split(":");
//        int min = Integer.parseInt(s[0]);
//        String[] ss = s[1].split("\\.");
//        int sec = Integer.parseInt(ss[0]);
//        int mill = Integer.parseInt(ss[1]);
//        return min * 60 * 1000 + sec * 1000 + mill * 10;
//    }
//    public String getLrcContent()
//    {
//        return lmlist.getContent();
//    }
}
