package com.xixi.request.tool.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

/**
 * @author shengchengchao
 * @Description 对于流的处理
 * @createTime 2021/3/14
 */
public class StreamUtil {


    public static byte[] read(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int num = 0;
            while ((num = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, num);
            }
            baos.flush();
            return baos.toByteArray();
        }finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * 对象转数组
     * @param obj
     * @return
     */
    public  static byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

}
