package com.wantoper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// 版本2
public class startmain {

	public static void main(String[] args) throws Exception {
		InetAddress host = InetAddress.getLocalHost();
		String ip = host.getHostAddress();

		System.out.println("此设备ip为："+ip);

		String[] ipsz =ip.split("\\.");
		String startip = ipsz[0] + "."+ipsz[1] + "."+ipsz[2] + "."+0;
		String endip =ipsz[0] + "."+ipsz[1] + "."+ipsz[2] + "."+255;


		List<List> returnlist = snifferip(startip,endip,false);
		List<String> successlist = returnlist.get(0);
		List<String> faillist = returnlist.get(1);

		System.out.println("=========存在的ip=========");
		System.out.println("个数:"+successlist.size());
		System.out.println("存在的ip如下:");
		for (String s : successlist) {
			System.out.println(s);
		}

		System.out.println("=========不存在的ip=========");
		System.out.println("个数:"+faillist.size());
		System.out.println("不存在的ip如下:");
		for (String s : faillist) {
			System.out.println(s);
		}
	}


	public static List<List> snifferip(String startip,String endip,boolean print) throws InterruptedException {
		System.out.println("---------扫描开始-----------");
		System.out.println("扫描范围:"+startip+" - "+endip);
		String[] startipsplit = startip.split("\\.");
		String[] endipsplit = endip.split("\\.");

		int satart =Integer.parseInt(startipsplit[3]);
		int end = Integer.parseInt(endipsplit[3]);


		System.out.println("共"+(end-satart)+"个ip");
		List<String> faillist = new ArrayList<>();
		List<String> successlist = new ArrayList<>();
		ThreadPoolExecutor threadPool= new ThreadPoolExecutor(100, 200, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		AtomicInteger atomicI =new AtomicInteger();
		for(int x =satart;x<end;x++) {
			String ips = startipsplit[0] + "."+startipsplit[1] + "."+startipsplit[2] + "."+x;

			threadPool.execute(new Runnable() {
				public void run() {
					//是否开启打印日志
					if(print){
						int n = 0;
						synchronized (atomicI) {
							n = atomicI.incrementAndGet();
						}
						System.out.println("测试第"+n+"个" +ips);
					}
					//开始扫描
					if(ping(ips)) {
						synchronized (successlist) {
							successlist.add(ips);
						}
					}else{
						synchronized (faillist) {
							faillist.add(ips);
						}
					}
				}
			});

		}
		threadPool.shutdown();
		//等待线程完全关闭
		while(!threadPool.awaitTermination(1, TimeUnit.HOURS)){

		}

		//将两个集合打包成一个大集合返回
		List<List> returnlist = new ArrayList<List>();
		returnlist.add(successlist);
		returnlist.add(faillist);
		return returnlist;
	}
	
	
	public static boolean ping(String ip) {
		boolean is = false;
		try {

			Process p = Runtime.getRuntime().exec("ping " + ip);
			BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while(true) {
					if (!((line = bf.readLine()) != null)) break;
					if(line.indexOf("TTL") != -1) {
						is = true;
					}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
		
	}

}
