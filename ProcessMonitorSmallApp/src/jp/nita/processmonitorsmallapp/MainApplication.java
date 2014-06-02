/*
 * Copyright 2011, 2012 Sony Corporation
 * Copyright (C) 2012 Sony Mobile Communications AB.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.nita.processmonitorsmallapp;

import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

public class MainApplication extends SmallApplication {

	final static Handler handler = new Handler();

	private Heart heart=null;

	int last2=0;
	int last3=0;
	int last4=0;
	int last5=0;

	@Override
	public void onCreate() {
		super.onCreate();
		setContentView(R.layout.main);
		setTitle(R.string.app_name);

		SmallAppWindow.Attributes attr = getWindow().getAttributes();
		attr.minWidth = 480; /* The minimum width of the application, if it's resizable.*/
		attr.minHeight = 480; /*The minimum height of the application, if it's resizable.*/
		attr.width = 480;  /*The requested width of the application.*/
		attr.height = 480;  /*The requested height of the application.*/
		// attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;   /*Use this flag to enable the application window to be resizable*/
		//        attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;  /*Use this flag to remove the titlebar from the window*/
		//        attr.flags |= SmallAppWindow.Attributes.FLAG_HARDWARE_ACCELERATED;  /* Use this flag to enable hardware accelerated rendering*/

		getWindow().setAttributes(attr); /*setting window attributes*/

		heart=new Heart();
		heart.start();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	class Heart extends Thread implements Runnable{
		private boolean awake=true;
		private boolean alive=true;
		public void run(){
			while(alive){
				try{
					Thread.sleep(1000);
					if(awake) heartbeat();
				}catch(InterruptedException e){
					die();
				}
			}
		}
		public void wake(){
			awake=true;
		}
		public void sleep(){
			awake=false;
		}
		public void die(){
			alive=false;
		}
	}

	public void heartbeat(){
		String [] cmdArgs = {"/system/bin/cat","/proc/stat"};
		String cpuLine =  "";
		StringBuffer cpuBuffer = new StringBuffer();
		ProcessBuilder cmd = new ProcessBuilder(cmdArgs);

		try {
			Process process = cmd.start();
			InputStream in  = process.getInputStream();
			byte[] lineBytes = new byte[1024];
			while(in.read(lineBytes) != -1 ) {
				cpuBuffer.append(new String(lineBytes));
			}
			in.close();
		}catch (IOException e) {

		}
		cpuLine = cpuBuffer.toString();
		int start = cpuLine.indexOf("cpu");
		int end = cpuLine.indexOf("cpu0");
		cpuLine = cpuLine.substring(start, end);

		Log.i("CPU_VALUES_LINE",cpuLine);
		String[] values = getValues(cpuLine,"\\s");

		int current2 = Integer.parseInt(values[2]);
		int current3 = Integer.parseInt(values[3]);
		int current4 = Integer.parseInt(values[4]);
		int current5 = Integer.parseInt(values[5]);

		int usage2 = current2 - last2;
		int usage3 = current3 - last3;
		int usage4 = current4 - last4;
		int usage5 = current5 - last5;
		int total = usage2 + usage3 + usage4 + usage5;

		String content ="user: "+Math.round(usage2*100/total)+"%\n"+
				"nice: "+Math.round(usage3*100/total)+"%\n"+
				"sys:  "+Math.round(usage4*100/total)+"%\n"+
				"idle: "+Math.round(usage5*100/total)+"%\n";
		final String cnt=content;
		new Thread(new Runnable(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						TextView percentage=(TextView)(findViewById(R.id.percentage));
						percentage.setText(cnt);
					}
				});
			}
		}).start();
		
		last2 = current2;
		last3 = current3;
		last4 = current4;
		last5 = current5;
	}

	public String[] getValues(String str,String sprt){
		String[] vals = str.split("\\s");
		return vals;
	}
}
