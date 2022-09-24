package com.lagou.edu.server.response;

import com.lagou.edu.server.utils.HttpProtocolUtil;
import com.lagou.edu.server.utils.StaticResourceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 封装Rsponse对象，需要依赖于OutputStream
 *
 * 该对象需要提供核心方法，输出html
 */
public class Response {
    private OutputStream outputStream;

    public Response() {
    }

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }


    // 使用输出流，输出指定字符串
    public void output(String content) throws IOException {
        outputStream.write(content.getBytes());
    }

    /**
     * path: request的url, 随后根据url来获取到静态资源的据对路径
     *                     进一步根据绝对路径读取该静态资源文件，最终通过输出流输出
     *       / ----> classes路径
     * @param path
     */
    public void outputHtml(String path) throws IOException {
        // 获取静态资源文件的绝对路径
        String absoluteResourcePath = StaticResourceUtil.getAbsolutePath(path);

        // 输出静态资源文件
        File file = new File(absoluteResourcePath);
        if(file.exists() && file.isFile()){
            // 读取静态资源文件，输出静态资源
            StaticResourceUtil.outputStaticResouce(new FileInputStream(file), outputStream);
        }else{
            // 输出404
            output(HttpProtocolUtil.getHttpHeader404());
        }
    }
}
