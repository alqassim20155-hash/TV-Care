package com.qassim.tvlauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class CareActivity extends Activity {
    private final int BACK=0xff071326, PANEL=0xff112743, ACCENT=0xff27b5ee, MUTED=0xffadc5df;
    private LinearLayout root, dashboard;
    private int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+.5f);}
    private GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));d.setStroke(dp(1),0xff284a6b);return d;}
    private TextView text(String value,int size,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private LinearLayout line(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);render();}
    private void render(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BACK);root.setPadding(dp(28),dp(18),dp(28),dp(16));setContentView(root);
        LinearLayout header=line();root.addView(header,new LinearLayout.LayoutParams(-1,dp(68)));
        TextView title=text("✦  SMART TV CARE",29,true);title.setTextColor(ACCENT);header.addView(title,new LinearLayout.LayoutParams(0,-1,1));
        addHeaderButton(header,"✦  AI Doctor",()->startActivity(new Intent(this,AiDoctorActivity.class)));addHeaderButton(header,"↻  تحديث",()->render());addHeaderButton(header,"⌂  الرئيسية",()->finish());
        TextView subtitle=text("مركز العناية بالتلفزيون  •  تشخيص سريع وأدوات صيانة آمنة",16,false);subtitle.setTextColor(MUTED);root.addView(subtitle);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        dashboard=new LinearLayout(this);dashboard.setOrientation(LinearLayout.VERTICAL);dashboard.setPadding(0,dp(18),0,dp(8));scroll.addView(dashboard);
        TextView section=text("نظرة عامة على الجهاز",21,true);dashboard.addView(section);
        LinearLayout cards=line();LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(130));cp.topMargin=dp(12);dashboard.addView(cards,cp);
        ActivityManager.MemoryInfo memory=new ActivityManager.MemoryInfo();((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(memory);
        StatFs fs=new StatFs(Environment.getDataDirectory().getAbsolutePath());long total=fs.getTotalBytes(),free=fs.getAvailableBytes();
        String network="غير متصل";try{ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);NetworkInfo info=cm.getActiveNetworkInfo();if(info!=null&&info.isConnected())network=info.getType()==ConnectivityManager.TYPE_WIFI?"Wi-Fi متصل":"شبكة متصلة";}catch(Exception ignored){}
        Intent battery=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));String batteryStatus="غير متاح";if(battery!=null){int level=battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);int scale=battery.getIntExtra(BatteryManager.EXTRA_SCALE,-1);if(level>=0&&scale>0)batteryStatus=(level*100/scale)+"%";}
        card(cards,"الشبكة",network,"اتصال الجهاز");card(cards,"التخزين",fmt(total-free)+" / "+fmt(total),"المستخدم / الإجمالي");card(cards,"الذاكرة",fmt(memory.availMem)+" متاح","RAM المتوفرة");card(cards,"البطارية",batteryStatus,"إن كانت مدعومة");
        TextView detail=text("معلومات النظام",21,true);LinearLayout.LayoutParams dt=new LinearLayout.LayoutParams(-1,dp(54));dt.topMargin=dp(16);dashboard.addView(detail,dt);
        LinearLayout sys=line();dashboard.addView(sys,new LinearLayout.LayoutParams(-1,dp(100)));
        card(sys,"الجهاز",Build.MANUFACTURER+"  "+Build.MODEL,"الشركة والطراز");card(sys,"Android",Build.VERSION.RELEASE,"API "+Build.VERSION.SDK_INT);card(sys,"وقت التشغيل",(android.os.SystemClock.elapsedRealtime()/3600000)+" ساعة","منذ آخر تشغيل");
        TextView tools=text("أدوات العناية والصيانة",21,true);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,dp(60));tp.topMargin=dp(10);dashboard.addView(tools,tp);
        LinearLayout a=line();dashboard.addView(a,new LinearLayout.LayoutParams(-1,dp(92)));
        action(a,"⚙  إعدادات النظام","إعدادات التلفزيون",Settings.ACTION_SETTINGS);
        action(a,"◉  إعدادات Wi-Fi","إدارة الاتصال",Settings.ACTION_WIFI_SETTINGS);
        action(a,"▣  إدارة التطبيقات","التطبيقات والتخزين",Settings.ACTION_APPLICATION_SETTINGS);
        LinearLayout b=line();LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(92));bp.topMargin=dp(12);dashboard.addView(b,bp);
        action(b,"◈  التخزين","فحص المساحة",Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        action(b,"✦  إمكانية الوصول","إعدادات التحكم",Settings.ACTION_ACCESSIBILITY_SETTINGS);
        TextView cache=button("▤  إعدادات التطبيق ومسح ذاكرته المؤقتة",PANEL,17);LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,-1,1);xp.rightMargin=dp(12);b.addView(cache,xp);
        cache.setOnClickListener(v->{try{Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()));startActivity(i);}catch(Exception e){toast("الإعداد غير متاح على هذا الجهاز");}});
        TextView note=text("ملاحظة: أدوات الصيانة تفتح صفحات النظام الرسمية؛ لا يتم حذف الملفات أو إيقاف التطبيقات أو إعادة تشغيل الجهاز تلقائيًا.",14,false);note.setTextColor(MUTED);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,dp(56));np.topMargin=dp(12);dashboard.addView(note,np);
    }
    private String fmt(long bytes){return String.format(Locale.US,"%.1f GB",bytes/1073741824.0);}
    private void card(LinearLayout parent,String heading,String value,String desc){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(12),dp(12),dp(8));box.setBackground(shape(PANEL));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.rightMargin=dp(12);parent.addView(box,p);TextView h=text(heading,15,false);h.setTextColor(MUTED);box.addView(h);TextView v=text(value,19,true);v.setSingleLine(true);v.setEllipsize(android.text.TextUtils.TruncateAt.END);box.addView(v);TextView d=text(desc,12,false);d.setTextColor(MUTED);box.addView(d);}
    private TextView button(String label,int color,int size){TextView t=text(label,size,true);t.setGravity(Gravity.CENTER);t.setBackground(shape(color));t.setFocusable(true);t.setOnFocusChangeListener((v,focused)->{GradientDrawable bg=shape(focused?0xff175a8e:color);bg.setStroke(dp(focused?3:1),focused?ACCENT:0xff284a6b);v.setBackground(bg);v.setScaleX(focused?1.025f:1f);v.setScaleY(focused?1.025f:1f);});return t;}
    private void addHeaderButton(LinearLayout header,String label,Runnable click){TextView t=button(label,PANEL,15);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(145),dp(50));p.leftMargin=dp(12);header.addView(t,p);t.setOnClickListener(v->click.run());}
    private void action(LinearLayout parent,String title,String hint,String action){TextView t=button(title+"\n"+hint,PANEL,16);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.rightMargin=dp(12);parent.addView(t,p);t.setOnClickListener(v->{try{startActivity(new Intent(action));}catch(Exception e){toast("هذا الإعداد غير متاح على جهازك");}});}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
