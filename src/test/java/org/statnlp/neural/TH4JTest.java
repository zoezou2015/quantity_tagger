package org.statnlp.neural;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.neural.util.LuaFunctionHelper;

import com.naef.jnlua.LuaState;
import com.sun.jna.Library;
import com.sun.jna.Native;

import th4j.Tensor.DoubleTensor;

public class TH4JTest {

	public static String LUA_VERSION = "5.2";
	private static LuaState L;
	private static void configureJNLua() {
		System.setProperty("jna.library.path","./nativeLib");
		System.setProperty("java.library.path", "./nativeLib:" + System.getProperty("java.library.path"));
		Field fieldSysPath = null;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String jnluaLib = null;
		if (LUA_VERSION.equals("5.2")) {
			jnluaLib = "libjnlua52";
		} else if (LUA_VERSION.equals("5.1")) {
			jnluaLib = "libjnlua5.1";
		}
		if (NetworkConfig.OS.equals("osx")) {
			jnluaLib += ".jnilib";
		} else if (NetworkConfig.OS.equals("linux")) {
			jnluaLib += ".so";
		}
		Native.loadLibrary(jnluaLib, Library.class);
		
		L = new LuaState();
		L.openLibs();
		
		try {
			L.load(Files.newInputStream(Paths.get("nn-crf-interface/neural_server/test.lua")),"test.lua","bt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		L.call(0,0);
	}
	
	public static void main(String[] args) {
		//System.out.println((int)27247577);
		configureJNLua();
		
		DoubleTensor test = new DoubleTensor();
		test.resize(2);
		double[] testArr = new double[]{3, 4};
		test.storage().copy(testArr);
		Object[] myArgs = new Object[]{test};
		LuaFunctionHelper.execLuaFunction(L, "testEmptyTensor", myArgs, new Class[0]);
		
		double[] weights = new double[100];
		for (int i = 0; i< weights.length; i++)
			weights[i]= i;
		DoubleTensor tensor = new DoubleTensor(weights);
		//LongTensor longTensor = new LongTensor(10);
		weights[50] = 1000;
		double[] arr = tensor.storage().getRawData().getDoubleArray(0, (int)tensor.nElement());
		System.out.println(arr[50]);
//		tensor.storage().copy(weights);
//		double[] arr = tensor.storage().getRawData().getDoubleArray(0, (int)tensor.nElement());
//		for(int i = 0; i< arr.length; i++){
//			System.out.println(arr[i]);
//		}
		
	}
	
	public static double[] getArray(DoubleTensor t, double[] buf) {
		if (buf == null || buf.length != t.nElement()) {
			buf = new double[(int) t.nElement()];
        }
		t.storage().getRawData().read(0, buf, 0, (int) t.nElement());
		return buf;
	}
}
