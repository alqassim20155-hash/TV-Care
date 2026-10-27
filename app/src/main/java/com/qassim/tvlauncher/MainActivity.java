package com.qassim.tvlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import java.io.*;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.net.Uri;
import android.os.Build;
import android.icu.util.IslamicCalendar;

public class MainActivity extends Activity {
    private static final int WHITE=0xfff5f8ff, MUTED=0xffc5d5ec, GLASS=0xc51a2948, CYAN=0xff63e6ff;
    private LinearLayout list; private TextView clock; private boolean onlyFav=false;
    private RemoteServer remote; private TextView remoteInfo;
    private final Object orderLock=new Object();
    private final Handler handler=new Handler();
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    private TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(WHITE);t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(null,1);return t;}
    private GradientDrawable panel(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),0x66ffffff);return d;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);render();remote=new RemoteServer();remote.start();}
    private void focus(View v,int base){v.setFocusable(true);v.setOnFocusChangeListener((view,f)->{GradientDrawable d=panel(f?0xee255d86:base,17);d.setStroke(dp(f?3:1),f?CYAN:0x55ffffff);view.setBackground(d);view.animate().scaleX(f?1.045f:1f).scaleY(f?1.045f:1f).setDuration(130).start();});}
    private TextView button(String title,int color){TextView t=text(title,17,true);t.setGravity(Gravity.CENTER);t.setBackground(panel(color,17));focus(t,color);return t;}
    private void render(){
        FrameLayout frame=new FrameLayout(this);frame.setBackground(new Landscape());setContentView(frame);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(35),dp(16),dp(35),dp(18));frame.addView(root,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=horizontal();root.addView(header,new LinearLayout.LayoutParams(-1,dp(64)));
        TextView brand=text("◈  SMART TV",30,true);header.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
        clock=text("",16,true);header.addView(clock);updateClock();
        TextView settings=button("⚙  الإعدادات",GLASS);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dp(145),dp(48));hp.leftMargin=dp(20);header.addView(settings,hp);settings.setOnClickListener(v->openSettings());
        TextView welcome=text("مساحتك للترفيه  •  تحكم بالتطبيقات من التلفزيون أو الجوال",18,false);welcome.setTextColor(MUTED);root.addView(welcome);
        remoteInfo=text("لوحة التحكم: جارٍ تشغيل الخدمة...",14,false);remoteInfo.setTextColor(CYAN);root.addView(remoteInfo);
        LinearLayout menu=horizontal();LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(90));mp.topMargin=dp(12);root.addView(menu,mp);
        String[] labels={"▦  جميع التطبيقات","★  المفضلة","✦  TV Care","⚙  إعدادات النظام"};
        for(int i=0;i<labels.length;i++){final int idx=i;TextView b=button(labels[i],i==2?0xe0187899:GLASS);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(68),1);p.rightMargin=dp(14);menu.addView(b,p);b.setOnClickListener(v->{if(idx<2){onlyFav=idx==1;populate();}else if(idx==2)startActivity(new Intent(this,CareActivity.class));else openSettings();});}
        TextView heading=text("تطبيقاتك",23,true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));root.addView(heading,lp);
        TextView hint=text("اضغط مطولًا للمفضلة  •  رتّب التطبيقات من لوحة تحكم الجوال",14,false);hint.setTextColor(MUTED);root.addView(hint);
        ScrollView sc=new ScrollView(this);sc.setClipToPadding(false);sc.setFillViewport(true);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,0,1);sp.topMargin=dp(12);root.addView(sc,sp);
        list=new LinearLayout(this);list.setOrientation(1);list.setPadding(dp(5),dp(5),dp(5),dp(12));sc.addView(list);populate();
    }
    private void populate(){
        list.removeAllViews();PackageManager pm=getPackageManager();List<ApplicationInfo> apps=new ArrayList<>();
        for(ApplicationInfo a:pm.getInstalledApplications(0))if(!a.packageName.equals(getPackageName())&&launch(a.packageName)!=null&&(!onlyFav||getPreferences(0).getBoolean(a.packageName,false)))apps.add(a);
        List<String> saved=readOrder();
        Collections.sort(apps,(a,b)->{int ia=saved.indexOf(a.packageName),ib=saved.indexOf(b.packageName);if(ia>=0||ib>=0){if(ia<0)return 1;if(ib<0)return -1;return Integer.compare(ia,ib);}return String.CASE_INSENSITIVE_ORDER.compare(a.loadLabel(pm).toString(),b.loadLabel(pm).toString());});
        if(apps.isEmpty()){TextView e=text("لا توجد تطبيقات هنا. أضف تطبيقات إلى المفضلة بالضغط المطول.",18,false);list.addView(e);return;}
        LinearLayout row=null;
        for(int i=0;i<apps.size();i++){
            if(i%5==0){row=horizontal();LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(122));rp.bottomMargin=dp(15);list.addView(row,rp);}
            ApplicationInfo app=apps.get(i);boolean favorite=getPreferences(0).getBoolean(app.packageName,false);
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(1);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(8),dp(8),dp(8),dp(8));tile.setBackground(panel(GLASS,17));focus(tile,GLASS);
            ImageView icon=new ImageView(this);try{icon.setImageDrawable(app.loadIcon(pm));}catch(Exception ignored){}tile.addView(icon,new LinearLayout.LayoutParams(dp(54),dp(54)));
            TextView name=text((favorite?"★ ":"")+app.loadLabel(pm).toString(),15,true);name.setGravity(Gravity.CENTER);name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,dp(34));np.topMargin=dp(5);tile.addView(name,np);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(114),1);p.rightMargin=dp(15);row.addView(tile,p);
            tile.setOnClickListener(v->{Intent intent=launch(app.packageName);if(intent!=null)try{startActivity(intent);}catch(Exception e){Toast.makeText(this,"تعذر فتح التطبيق",Toast.LENGTH_SHORT).show();}});
            tile.setOnLongClickListener(v->{boolean next=!getPreferences(0).getBoolean(app.packageName,false);getPreferences(0).edit().putBoolean(app.packageName,next).apply();Toast.makeText(this,next?"تمت الإضافة للمفضلة":"تمت الإزالة من المفضلة",Toast.LENGTH_SHORT).show();populate();return true;});
        }
    }
    private Intent launch(String pkg){PackageManager pm=getPackageManager();Intent i=pm.getLeanbackLaunchIntentForPackage(pkg);return i!=null?i:pm.getLaunchIntentForPackage(pkg);}
    private void openSettings(){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception e){Toast.makeText(this,"الإعدادات غير متاحة",Toast.LENGTH_SHORT).show();}}
    private void updateClock(){if(clock==null)return;
        Date now=new Date();String greg=new SimpleDateFormat("EEEE d MMMM yyyy",new Locale("ar")).format(now);
        String hijri="";
        if(Build.VERSION.SDK_INT>=24){IslamicCalendar h=new IslamicCalendar(new Locale("ar"));h.setTime(now);String[] months={"محرم","صفر","ربيع الأول","ربيع الآخر","جمادى الأولى","جمادى الآخرة","رجب","شعبان","رمضان","شوال","ذو القعدة","ذو الحجة"};hijri=h.get(IslamicCalendar.DAY_OF_MONTH)+" "+months[h.get(IslamicCalendar.MONTH)]+" "+h.get(IslamicCalendar.YEAR)+" هـ";}
        else {hijri="الهجري يتطلب Android 7+";}
        clock.setText(new SimpleDateFormat("HH:mm",Locale.getDefault()).format(now)+"  •  "+greg+"  |  "+hijri);
        handler.removeCallbacksAndMessages(null);handler.postDelayed(this::updateClock,30000);
    }
    private List<String> readOrder(){synchronized(orderLock){String raw=getPreferences(0).getString("app_order","");return new ArrayList<>(raw.isEmpty()?Collections.emptyList():Arrays.asList(raw.split(",")));}}
    private void saveOrder(List<String> order){synchronized(orderLock){getPreferences(0).edit().putString("app_order",android.text.TextUtils.join(",",order)).apply();}runOnUiThread(this::populate);}
    private String localIp(){try{WifiManager w=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);int ip=w.getConnectionInfo().getIpAddress();if(ip!=0)return Formatter.formatIpAddress(ip);}catch(Exception ignored){}return "عنوان التلفزيون";}
    private String esc(String v){return v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
    private String encode(String v){try{return java.net.URLEncoder.encode(v,"UTF-8");}catch(Exception e){return v;}}
    private class RemoteServer extends Thread{
        private ServerSocket server; private volatile boolean running=true;
        private final String token=String.format(Locale.US,"%06d",new java.security.SecureRandom().nextInt(1000000));
        public void run(){try{server=new ServerSocket(8765);server.setSoTimeout(1500);runOnUiThread(()->remoteInfo.setText("التحكم من الجوال: http://"+localIp()+":8765  |  رمز الدخول: "+token));
            while(running){try{Socket c=server.accept();c.setSoTimeout(4000);new Thread(()->serve(c)).start();}catch(SocketTimeoutException ignored){}}
        }catch(Exception e){runOnUiThread(()->remoteInfo.setText("تعذر تشغيل التحكم الشبكي: "+e.getMessage()));}}
        void stopServer(){running=false;try{if(server!=null)server.close();}catch(Exception ignored){}}
        private void serve(Socket c){try(Socket socket=c){BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));String request=in.readLine();if(request==null)return;String[] parts=request.split(" ");if(parts.length<2)return;String path=parts[1];String line;while((line=in.readLine())!=null&&!line.isEmpty()){};
            String response;int status=200;
            if(!parts[0].equals("GET")){status=405;response="Method not allowed";}
            else if(path.equals("/")||path.startsWith("/?")){response=page(path);}
            else if(!path.startsWith("/action?")){status=404;response="Not found";}
            else {Map<String,String> q=query(path);if(!token.equals(q.get("pin"))){status=403;response="Invalid PIN";}else{String pkg=q.getOrDefault("pkg","");String op=q.getOrDefault("op","");if(!validPackage(pkg)){status=400;response="Invalid app";}else{if(op.equals("up")||op.equals("down")){List<String> order=readOrder();if(!order.contains(pkg))order.add(pkg);int idx=order.indexOf(pkg),target=idx+(op.equals("up")?-1:1);if(target>=0&&target<order.size())Collections.swap(order,idx,target);saveOrder(order);}else if(op.equals("favorite")){boolean old=getPreferences(0).getBoolean(pkg,false);getPreferences(0).edit().putBoolean(pkg,!old).apply();runOnUiThread(()->populate());}else if(op.equals("launch")){runOnUiThread(()->{Intent i=launch(pkg);if(i!=null)try{startActivity(i);}catch(Exception ignored){}});}else if(op.equals("uninstall")){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_DELETE,Uri.parse("package:"+pkg));try{startActivity(i);}catch(Exception ignored){Toast.makeText(MainActivity.this,"الحذف غير مدعوم",Toast.LENGTH_LONG).show();}});}response="<meta http-equiv='refresh' content='0;url=/?pin="+token+"'>";}}}
            byte[] data=response.getBytes("UTF-8");OutputStream out=socket.getOutputStream();out.write(("HTTP/1.1 "+status+" OK
Content-Type: text/html; charset=utf-8
Content-Security-Policy: default-src 'none'; style-src 'unsafe-inline'
Cache-Control: no-store
X-Content-Type-Options: nosniff
Content-Length: "+data.length+"
Connection: close

").getBytes("UTF-8"));out.write(data);out.flush();}catch(Exception ignored){}}
        private Map<String,String> query(String path){Map<String,String> map=new HashMap<>();int ix=path.indexOf('?');if(ix<0)return map;for(String part:path.substring(ix+1).split("&")){String[] a=part.split("=",2);try{map.put(java.net.URLDecoder.decode(a[0],"UTF-8"),a.length>1?java.net.URLDecoder.decode(a[1],"UTF-8"):"");}catch(Exception ignored){}}return map;}
        private boolean validPackage(String pkg){if(pkg.equals(getPackageName()))return false;return launch(pkg)!=null;}
        private String page(String path){String pin=query(path).getOrDefault("pin","");StringBuilder html=new StringBuilder("<!doctype html><html lang='ar' dir='rtl'><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>SMART TV Remote</title><style>body{background:#08162b;color:#f5f9ff;font:16px sans-serif;padding:20px;max-width:760px;margin:auto}h1{color:#70dcff}article{background:#172b48;border-radius:14px;margin:12px 0;padding:14px}a,button{display:inline-block;color:white;background:#215c89;padding:9px;margin:4px;border:0;border-radius:9px;text-decoration:none}input{padding:12px;border-radius:9px}small{color:#aac1db}</style><h1>SMART TV — التحكم عن بُعد</h1>");
            if(!token.equals(pin)){html.append("<p>أدخل رمز الدخول الظاهر على التلفزيون:</p><form method='get'><input name='pin' type='password' inputmode='numeric' required maxlength='6'><button>دخول</button></form>");}
            else {html.append("<p>يمكنك ترتيب التطبيقات، فتحها، إضافتها للمفضلة وطلب حذفها. الحذف يحتاج تأكيدًا على التلفزيون.</p>");PackageManager pm=getPackageManager();List<ApplicationInfo> apps=new ArrayList<>();for(ApplicationInfo a:pm.getInstalledApplications(0))if(validPackage(a.packageName))apps.add(a);List<String> order=readOrder();apps.sort((a,b)->{int x=order.indexOf(a.packageName),y=order.indexOf(b.packageName);if(x>=0||y>=0)return x<0?1:y<0?-1:Integer.compare(x,y);return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());});for(ApplicationInfo a:apps){String pkg=encode(a.packageName);html.append("<article><b>").append(esc(a.loadLabel(pm).toString())).append("</b><br><small>").append(esc(a.packageName)).append("</small><div>");String[] ops={"up","down","favorite","launch","uninstall"},labels={"↑ للأعلى","↓ للأسفل","★ المفضلة","فتح","إزالة التثبيت"};for(int i=0;i<ops.length;i++)html.append("<a href='/action?pin=").append(token).append("&op=").append(ops[i]).append("&pkg=").append(pkg).append("'>").append(labels[i]).append("</a>");html.append("</div></article>");}}
            return html.append("</html>").toString();}
    }
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(remote!=null)remote.stopServer();super.onDestroy();}
    // Scenic dusk landscape, drawn locally: no network, permissions, or external image assets.
    private class Landscape extends Drawable {
        private final Paint p=new Paint(3);
        @Override public void draw(Canvas c){android.graphics.Rect b=getBounds();float w=b.width(),h=b.height();
            p.setShader(new LinearGradient(0,0,0,h,new int[]{0xff06152e,0xff143d65,0xff456a83,0xff0a1c35},null,Shader.TileMode.CLAMP));c.drawRect(b,p);p.setShader(null);
            p.setColor(0x88d7efff);c.drawCircle(w*.79f,h*.23f,h*.085f,p);p.setColor(0x226bd2ff);c.drawCircle(w*.79f,h*.23f,h*.14f,p);
            mountain(c,w,h,.66f,0xff244663,new float[]{0,.70f,.13f,.45f,.26f,.63f,.39f,.35f,.54f,.66f,.71f,.41f,.86f,.60f,1,.42f});
            mountain(c,w,h,.78f,0xff122d4b,new float[]{0,.61f,.16f,.72f,.29f,.52f,.43f,.76f,.62f,.51f,.78f,.74f,.91f,.54f,1,.67f});
            p.setColor(0x77234d69);c.drawRect(0,h*.79f,w,h,p);p.setShader(new LinearGradient(0,h*.78f,0,h,new int[]{0x00305a7d,0x9920385b,0xff06162e},null,Shader.TileMode.CLAMP));c.drawRect(0,h*.78f,w,h,p);p.setShader(null);
            p.setShader(new LinearGradient(0,0,0,h,0x33030a1b,0x99040b20,Shader.TileMode.CLAMP));c.drawRect(b,p);p.setShader(null);
        }
        private void mountain(Canvas c,float w,float h,float bottom,int color,float[] pts){Path path=new Path();path.moveTo(0,h*bottom);for(int i=0;i<pts.length;i+=2)path.lineTo(pts[i]*w,pts[i+1]*h);path.lineTo(w,h);path.lineTo(0,h);path.close();p.setColor(color);c.drawPath(path,p);}
        @Override public void setAlpha(int a){} @Override public void setColorFilter(android.graphics.ColorFilter f){} @Override public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}
    }
}
