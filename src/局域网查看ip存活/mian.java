package 局域网查看ip存活;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// 版本1
public class mian {

	public static void main(String[] args) throws Exception {
		InetAddress host = InetAddress.getLocalHost();
		String ip = host.getHostAddress();
		final Object iad = new Object();
		
		String[] ipsz = ip.split("\\.");
		ip = ipsz[0] + "."+ipsz[1] + "."+ipsz[2] + ".";
		
		List<String> list = new ArrayList<>();
		System.out.println("---------测试开始-----------");

		
		ThreadPoolExecutor threadPool= new ThreadPoolExecutor(100, 200, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		AtomicInteger atomicI =new AtomicInteger();
		for(int x =1;x<=255;x++) {
			String ips = ip;
			
			threadPool.execute(new Runnable() {
				
				public void run() {
					int p = 0;
					
					synchronized (atomicI) {
						p = atomicI.incrementAndGet();			
					}
					System.out.println("测试第"+p+"个");	
					try {
						if(ping(ips+p)) {
							synchronized (list) {
								list.add(ips+p);
							}
						}
					} catch (Exception e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}					
				}
			});
		}
		threadPool.shutdown();
		
		if(threadPool.awaitTermination(1, TimeUnit.HOURS)) {
			System.out.println("---------测试结束-----------");
			System.out.println("存在的ip如下");
			for(String str:list) {
				System.out.println(str);
			}
			
			System.out.println("ip总数为："+list.size());
		}
	}
	
	
	public static boolean ping(String ip) throws Exception {
		boolean is = false;
		Process p = Runtime.getRuntime().exec("ping " + ip);
		BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while((line = bf.readLine()) != null) {
			if(line.indexOf("TTL") != -1) {
				is = true;
			}
		}
		
		return is;
		
	}

}
