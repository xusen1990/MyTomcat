package com.lagou.edu.server;

import cn.hutool.core.util.ZipUtil;
import com.lagou.edu.server.mapper.MappedContext;
import com.lagou.edu.server.mapper.MappedHost;
import com.lagou.edu.server.mapper.MappedWrapper;
import com.lagou.edu.server.request.RequestProcessor;
import com.lagou.edu.server.servlet.HttpServlet;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Minicat的主类
 */
public class Bootstrap {


    //定义socket监听的端口号
    private int port;

    // appBase的路径
    private String appBase;

    // host的值
    private String host;

    // 存储url - servlet的容器
    //private Map<String, HttpServlet> servletMap = new HashMap<>();
    private Map<String, MappedHost>  servletMap = new HashMap<>();

    // 线程池
    private ThreadPoolExecutor threadPoolExecutor;


    /**
     * Minicat 程序的启动入口
     * @param args
     */
    public static void main(String[] args) {

        Bootstrap bootstrap = new Bootstrap();
        try {
            // 启动Minicat
            bootstrap.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Minicat启动需要初始化展开的一些操作
     */
    public void start() throws Exception {


        // 加载解析server.xml
        loadServerXml();

//        System.out.println("====>>>post: " + this.port);
//        System.out.println("====>>>host: " + this.host);
//        System.out.println("====>>>appBase: " + this.appBase);


        // 解压appBase下的war包
        unpackWars();


        // 加载解析各个app中的web.xml，同时将class文件解析为servlet
        loadWebXmlAndServlet();


        // 初始化线程池
        initThreadPool();

        // 启动socket进行监听

        startSocketToListen();

    }

    private void startSocketToListen() {
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("======>>Minicat start on port: " + port);

            // 多线程改造(使用线程池)
            System.out.println("=======>>>>使用线程池进行多线程改造");
            while (true){
                Socket socket = serverSocket.accept();
                RequestProcessor requestProcessor = new RequestProcessor(socket, servletMap);

                threadPoolExecutor.execute(requestProcessor);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载解析各个app中的web.xml，同时将class文件解析为servlet
     */
    private void loadWebXmlAndServlet() {

        // 初始化mappedHost
        MappedHost mappedHost = new MappedHost();
        mappedHost.setName(this.host + ":" + this.port);

        try{
            File appBaseFolder = new File(this.appBase);

            // 对解压后的war文件进行遍历
            File[] appDirs = appBaseFolder.listFiles(File::isDirectory);

            if(appDirs != null && appDirs.length > 0){
                for(File appDir : appDirs){
                    String appName = appDir.getName();
                    String webXmlPath = appDir.getPath().replaceAll("\\\\","/") + "/" + "WEB-INF" + "/" + "web.xml";
                    String classPath = appDir.getPath().replaceAll("\\\\","/") + "/" + "WEB-INF" + "/" + "classes";
                    //System.out.println(appName);
                    //System.out.println(webXmlPath);
                    //System.out.println(classPath);
                    loadWebXmlAndServlet(mappedHost, appName, webXmlPath, classPath);
                    System.out.println("====>>> Load " + appName + " successfully.");
                }
            }

            this.servletMap.put(this.host + ":" + this.port, mappedHost);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载解析各个app中的web.xml，同时将class文件解析为servlet
     */
    private void loadWebXmlAndServlet(MappedHost mappedHost, String appName, String webXmlPath, String classPath) {


        // 初始化mappedContext
        String contextName = appName;
        Map<String, MappedContext> mappedContextMap = mappedHost.getMappedContextMap();
        MappedContext mappedContext = new MappedContext();
        mappedContext.setName(contextName);
        mappedContextMap.put(contextName, mappedContext);

        // 解析web.xml
        doLoadWebXmlAndServlet(mappedContext, webXmlPath, classPath);

    }

    /**
     * 真正加载解析各个app中的web.xml，同时将class文件解析为servlet
     */
    private void doLoadWebXmlAndServlet(MappedContext mappedContext, String webXmlPath, String classPath) {

        try {
            File webXmlFile = new File(webXmlPath);
            FileInputStream fileInputStream = new FileInputStream(webXmlFile);

            // 解析web.xml
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(fileInputStream);
            Element rootElement = document.getRootElement();
            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element = selectNodes.get(i);
                // <servlet-name>lagou</servlet-name>
                Element servletnameElement = (Element)element.selectSingleNode("servlet-name");
                String servletName = servletnameElement.getStringValue();
                // <servlet-class>LagouServlet</servlet-class>
                Element servletclassElement = (Element)element.selectSingleNode("servlet-class");
                String servletClass = servletclassElement.getStringValue();

                // 根据servlet-name的值找到url-pattern
                Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                // <url-pattern>/lagou</url-pattern>
                String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();

                // 加载servlet
                URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file:" + classPath + "/")});
                Class<HttpServlet> servletClazz = (Class<HttpServlet>)loader.loadClass(servletClass);
                HttpServlet httpServlet = servletClazz.newInstance();

                // 初始化mappedWrapper
                MappedWrapper mappedWrapper = new MappedWrapper();
                mappedWrapper.setName(urlPattern.replaceAll("/",""));
                mappedWrapper.setServlet(httpServlet);

                // 将mappedWrapper封装进mappedContext
                Map<String, MappedWrapper> mappedWrapperMap = mappedContext.getMappedWrapperMap();
                mappedWrapperMap.put(urlPattern.replaceAll("/",""), mappedWrapper);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 解压war包
     */
    private void unpackWars() {

        try{
            File appBaseFolder = new File(this.appBase);

            // 对appBase目录下的war包进行解压
            File[] files = appBaseFolder.listFiles();

            for(File file : files){
                if(file.getName().endsWith(".war")){
                    //System.out.println(file.getAbsoluteFile());
                    //System.out.println(file.getParent());
                    String unpackFolderName = file.getName().split("-")[0];

                    String unpackFolderPath = file.getParent().replaceAll("\\\\","/") + "/" + unpackFolderName;

                    ZipUtil.unzip(file.getAbsolutePath().replaceAll("\\\\","/"), unpackFolderPath);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }




    /**
     * 加载解析server.xml, 初始化port, host, appBase
     */
    private void loadServerXml() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();
        try{
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            // 解析端口号
            Element connectorNode = (Element) rootElement.selectNodes("//Connector").get(0);
            String portValue = connectorNode.attribute("port").getValue();
            this.port = Integer.parseInt(portValue);


            // 解析host 和 appBase, appBase配置时使用相对路径，这里转换成项目的绝对路径
            Element hostNode = (Element)rootElement.selectNodes("//Host").get(0);
            // 解析host
            this.host = hostNode.attribute("name").getValue();
            // 解析appBase
            String path = hostNode.attribute("appBase").getValue();
            String absolutePath = System.getProperty("user.dir");
            URLDecoder.decode(absolutePath, "UTF-8");
            //System.out.println("====》》》" + absolutePath);
            this.appBase = absolutePath.replaceAll("\\\\","/") + "/" + path;

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 初始化线程池
     */
    public void initThreadPool(){
        // 定义一个线程池
        int corePoolSize = 10;
        int maximumPoolSize = 50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        this.threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );
    }

}
