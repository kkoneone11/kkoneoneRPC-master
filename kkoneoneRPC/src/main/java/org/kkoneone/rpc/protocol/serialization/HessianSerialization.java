package org.kkoneone.rpc.protocol.serialization;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author：kkoneone11
 * @name：HessianSerialization
 * @Date：2023/12/11 21:15
 */
public class HessianSerialization implements RpcSerialization{
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        if(obj == null){
            throw new NullPointerException();
        }

        //创建一个byte数组
        byte[] results;
        HessianSerializerOutput hessianOutput;
        //创建ByteArrayOutputStream和Hessian 序列化来将对象写入字节数组中
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()){
            //创建序列化输出
            hessianOutput = new HessianSerializerOutput(os);
            //写入数据
            hessianOutput.writeObject(obj);
            //将缓冲区中的数据刷新到 os 中。
            hessianOutput.flush();
            results = os.toByteArray();
        }catch (Exception e){
            throw new SerializationException(e.toString());
        }

        return results;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        if (data == null) {
            throw new NullPointerException();
        }
        T result;

        try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            HessianSerializerInput hessianInput = new HessianSerializerInput(is);
            result = (T) hessianInput.readObject(clz);
        } catch (Exception e) {
            throw new SerializationException(e.toString());
        }

        return result;
    }
}
