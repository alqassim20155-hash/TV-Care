package com.qassim.tvlauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/** Offline, explainable rule-based diagnostics. No AI model, telemetry, or API keys. */
public class AiDoctorActivity extends Activity {
    private static final int BG=0xff081528, PANEL=0xff142c48, BLUE=0xff54c8fa;
    private String lastReport="";
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(15));return g;}
    private TextView label(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(15),dp(12),dp(15),dp(12));return t;}
    @Override public void onCreate(Bundle b){super.onCreate(b);render();}
    private void render(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);root.setPadding(dp(22),dp(12),dp(22),dp(12));setContentView(root);
        TextView title=label("SMART TV AI Doctor  •  فحص محلي",26,true);title.setTextColor(BLUE);root.addView(title);
        TextView notice=label("تشخيص ذكي قائم على قواعد محلية، وليس نموذج ذكاء اصطناعي توليدي. لا يرسل أي بيانات ولا يحتاج إنترنت.",15,false);root.addView(notice);
        LinearLayout bar=new LinearLayout(this);root.addView(bar);
        button(bar,"↻ إعادة الفحص",()->render());button(bar,"مشاركة التقرير",()->share());button(bar,"رجوع",()->finish());
        ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout body=new LinearLayout(this);body.setOrientation(1);scroll.addView(body);
        StringBuilder report=new StringBuilder("SMART TV AI Doctor — تقرير محلي\n");
        report.append(DateFormat.getDateTimeInstance().format(new Date())).append("\n");
        try {
            ActivityManager.MemoryInfo mem=new ActivityManager.MemoryInfo();((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(mem);
            StatFs fs=new StatFs(Environment.getDataDirectory().getAbsolutePath());
            long total=fs.getTotalBytes(), available=fs.getAvailableBytes();
            double storageFree=total>0?100.0*available/total:0;
            double ramFree=mem.totalMem>0?100.0*mem.availMem/mem.totalMem:0;
            String storageAdvice=storageFree<10?"المساحة منخفضة جدًا؛ راجع الملفات والتطبيقات غير المستخدمة من إعدادات النظام.":storageFree<20?"المساحة منخفضة نسبيًا؛ يفضل إبقاء مساحة حرة لتحديثات التطبيقات.":"المساحة المتاحة مناسبة وفق المؤشر التقريبي.";
            String ramAdvice=mem.lowMemory?"Android يبلغ عن ضغط في الذاكرة؛ أغلق التطبيقات غير الضرورية وأعد تشغيل الجهاز عند الحاجة.":ramFree<15?"الذاكرة المتاحة قليلة الآن؛ راقب الأداء، ولا يعني ذلك وحده وجود عطل.":"لا توجد إشارة مباشرة لضغط الذاكرة من Android.";
            section(body,report,"التخزين",String.format(Locale.US,"متاح %.1f من %.1f GB (%.0f%%)",available/1073741824.0,total/1073741824.0,storageFree),storageAdvice);
            section(body,report,"RAM",String.format(Locale.US,"متاح %.1f من %.1f GB (%.0f%%)",mem.availMem/1073741824.0,mem.totalMem/1073741824.0,ramFree),ramAdvice);
            boolean connected=false;String net="غير متصل";
            try {ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);NetworkInfo ni=cm.getActiveNetworkInfo();connected=ni!=null&&ni.isConnected();if(connected)net=ni.getTypeName();}catch(Exception ignored){net="تعذر قراءة الحالة";}
            section(body,report,"الشبكة",net,connected?"الشبكة المحلية متصلة؛ لم يتم اختبار الإنترنت أو سرعة الاتصال لأن الفحص يعمل دون إنترنت.":"افحص Wi-Fi أو كابل Ethernet وإعدادات الراوتر. هذا الفحص لا يحاول الاتصال بخادم خارجي.");
            long uptime=SystemClock.elapsedRealtime()/3600000;
            section(body,report,"النظام",Build.MANUFACTURER+" "+Build.MODEL+" — Android "+Build.VERSION.RELEASE+" — تشغيل "+uptime+" ساعة",uptime>168?"الجهاز يعمل منذ مدة طويلة؛ إعادة التشغيل الاختيارية قد تفيد عند وجود بطء.":"لا توجد مشكلة مؤكدة من مدة التشغيل وحدها.");
            section(body,report,"حدود التشخيص","فحص قراءة فقط","لا يمكن قياس حرارة المعالج أو صحة الشاشة أو أعطال التطبيقات الأخرى بدقة دون واجهات أو صلاحيات خاصة. لا يجري حذفًا أو تعديلًا للنظام.");
        } catch(Exception e){section(body,report,"تعذر إكمال الفحص","بعض بيانات النظام غير متاحة","قد تقيد نسخة Android بعض معلومات الجهاز. لا يلزم منح صلاحيات خطرة.");}
        lastReport=report.toString();
    }
    private void section(LinearLayout body,StringBuilder report,String heading,String value,String advice){
        LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setBackground(bg(PANEL));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);body.addView(card,p);
        TextView h=label(heading+"  •  "+value,19,true);h.setTextColor(BLUE);card.addView(h);card.addView(label("التقييم: "+advice,16,false));
        report.append("\n").append(heading).append(": ").append(value).append("\n").append(advice).append("\n");
    }
    private void button(LinearLayout bar,String s,Runnable action){TextView t=label(s,16,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(PANEL));t.setFocusable(true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(56),1);p.rightMargin=dp(10);bar.addView(t,p);t.setOnClickListener(v->action.run());}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,lastReport);startActivity(Intent.createChooser(i,"مشاركة تقرير الصيانة"));}
}
