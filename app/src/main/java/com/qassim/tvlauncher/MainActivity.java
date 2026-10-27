package com.qassim.tvlauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int NAVY = Color.rgb(4, 15, 39), PANEL = Color.rgb(12, 32, 64), BLUE = Color.rgb(12, 101, 237);
    private LinearLayout content;
    private TextView clock;
    private final Handler handler = new Handler();
    private boolean favoritesOnly = false;
    private final String prefs = "qassim_favorites";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        showHome();
    }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g;
    }
    private int dp(int n) { return (int)(getResources().getDisplayMetrics().density*n+.5f); }
    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER_VERTICAL); if(bold) t.setTypeface(null,Typeface.BOLD); return t;
    }
    private LinearLayout row() { LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);return r; }
    private void pad(View v,int x,int y) { v.setPadding(dp(x),dp(y),dp(x),dp(y)); }
    private void showHome() {
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(NAVY);pad(root,28,18);
        setContentView(root);
        LinearLayout header=row();root.addView(header,new LinearLayout.LayoutParams(-1,dp(75)));
        TextView brand=text("▣  QASSIM TV",32,true);brand.setTextColor(0xff4da1ff);header.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
        clock=text("",19,true);header.addView(clock);updateClock();
        TextView settings=tile("⚙  الإعدادات",PANEL,16);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(150),dp(54));sp.leftMargin=dp(16);header.addView(settings,sp);
        settings.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception e){Toast.makeText(this,"الإعدادات غير متاحة",Toast.LENGTH_SHORT).show();}});
        TextView subtitle=text("شاهد كل ما تحب  •  واجهة التلفزيون الذكية",16,false);subtitle.setTextColor(0xffa9c5e8);root.addView(subtitle);
        LinearLayout menu=row();LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(100));mp.topMargin=dp(12);root.addView(menu,mp);
        String[] categories={"▦  جميع التطبيقات","★  المفضلة","✦  Qassim TV Care","⚙  إعدادات النظام"};
        for(int i=0;i<categories.length;i++) { final int index=i; TextView b=tile(categories[i],i==0?BLUE:PANEL,18);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(78),1);p.rightMargin=dp(12);menu.addView(b,p);
            b.setOnClickListener(v->{if(index==0||index==1){favoritesOnly=index==1;populate();}else if(index==3){startActivity(new Intent(Settings.ACTION_SETTINGS));}else{startActivity(new Intent(this,CareActivity.class));}});
        }
        TextView label=text("تطبيقاتك  •  اضغط مطولاً لإضافة أو إزالة المفضلة",19,true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(45));root.addView(label,lp);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);scroll.addView(content);populate();
    }
    private TextView tile(String label,int color,int size) {
        TextView t=text(label,size,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(color,14));t.setFocusable(true);
        t.setOnFocusChangeListener((v,focused)->{GradientDrawable d=bg(focused?0xff1664c9:color,14);d.setStroke(dp(focused?3:1),focused?0xff71d9ff:0xff234a79);v.setBackground(d);v.setScaleX(focused?1.04f:1f);v.setScaleY(focused?1.04f:1f);});return t;
    }
    private void populate() {
        if(content==null)return;content.removeAllViews();
        PackageManager pm=getPackageManager(); List<ApplicationInfo> apps=pm.getInstalledApplications(0);
        List<ApplicationInfo> launchable=new ArrayList<>();
        for(ApplicationInfo a:apps) if(!a.packageName.equals(getPackageName()) && getLaunchIntent(a.packageName)!=null && (!favoritesOnly || getPreferences(0).getBoolean(a.packageName,false)))launchable.add(a);
        Collections.sort(launchable,Comparator.comparing(a->a.loadLabel(pm).toString().toLowerCase(Locale.ROOT)));
        if(launchable.isEmpty()){TextView empty=text("لا توجد تطبيقات في هذا القسم. افتح جميع التطبيقات وأضف مفضلاتك بالضغط المطول.",18,false);content.addView(empty);return;}
        LinearLayout current=null;
        for(int i=0;i<launchable.size();i++) {
            if(i%4==0){current=row();LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(88));rp.bottomMargin=dp(12);content.addView(current,rp);}
            ApplicationInfo app=launchable.get(i);
            String name=app.loadLabel(pm).toString();boolean fav=getPreferences(0).getBoolean(app.packageName,false);
            TextView b=tile((fav?"★  ":"▣  ")+name,PANEL,16);b.setSingleLine(true);b.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(78),1);p.rightMargin=dp(12);current.addView(b,p);
            b.setOnClickListener(v->{Intent intent=getLaunchIntent(app.packageName);if(intent!=null)try{startActivity(intent);}catch(Exception e){Toast.makeText(this,"تعذر فتح التطبيق",Toast.LENGTH_SHORT).show();}});
            b.setOnLongClickListener(v->{boolean next=!getPreferences(0).getBoolean(app.packageName,false);getPreferences(0).edit().putBoolean(app.packageName,next).apply();Toast.makeText(this,next?"أضيف إلى المفضلة":"أزيل من المفضلة",Toast.LENGTH_SHORT).show();populate();return true;});
        }
    }
    private Intent getLaunchIntent(String pkg) {
        PackageManager pm=getPackageManager();Intent i=pm.getLeanbackLaunchIntentForPackage(pkg);return i!=null?i:pm.getLaunchIntentForPackage(pkg);
    }
    private void updateClock(){if(clock==null)return;clock.setText(new SimpleDateFormat("HH:mm  •  EEEE، d MMMM yyyy",new Locale("ar")).format(new Date()));handler.removeCallbacksAndMessages(null);handler.postDelayed(this::updateClock,30000);}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
}
