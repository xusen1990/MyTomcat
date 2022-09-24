package com.lagou.edu.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class StaticResourceUtil {

    /**
     * 获取静态资源文件的绝对路径
     * @param path
     * @return
     */
    public static String getAbsolutePath(String path) throws UnsupportedEncodingException {
        String absolutePath = StaticResourceUtil.class.getClassLoader().getResource("").getPath();
        // 解决获取路径命中中文乱码问题
        absolutePath = URLDecoder.decode(absolutePath, "UTF-8");
        // 考虑Linux条件下的系统分割符
        return (absolutePath.replaceAll("\\\\","/") + path);
    }


    /**
     * 读取静态资源文件输入流，通过输出流输出
     * @param inputStream
     * @param outputStream
     */
    public static void outputStaticResouce(InputStream inputStream, OutputStream outputStream) throws IOException {

        int count = 0;
        while(count == 0){
            count = inputStream.available();
        }

        int resourceSize = count;
        // 输出http请求头
        outputStream.write(HttpProtocolUtil.getHttpHeader200(resourceSize).getBytes());

        // 输出具体内容
        // 读取内容输出
        long written = 0; // 已经读取的内容长度
        int byteSize = 1024; // 计划每次缓冲的长度
        byte[] bytes = new byte[byteSize];

        while(written < resourceSize){
            if(written + byteSize > resourceSize){  // 说明剩余未读取大小不足一个1024长度，那就是按真实长度处理
                byteSize = (int) (resourceSize - written); // 剩余的文件长度
            }
            inputStream.read(bytes);
            outputStream.write(bytes);

            outputStream.flush();
            written += byteSize;
        }
    }
}
