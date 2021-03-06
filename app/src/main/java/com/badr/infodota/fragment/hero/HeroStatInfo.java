package com.badr.infodota.fragment.hero;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badr.infodota.R;
import com.badr.infodota.api.heroes.Hero;
import com.badr.infodota.api.heroes.HeroStats;
import com.badr.infodota.util.FileUtils;
import com.badr.infodota.util.ResourceUtils;
import com.badr.infodota.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * User: ABadretdinov
 * Date: 15.01.14
 * Time: 14:43
 */
public class HeroStatInfo extends Fragment {
    protected ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Hero hero;
    private GifImageView imageView;

    public static HeroStatInfo newInstance(Hero hero) {
        HeroStatInfo fragment = new HeroStatInfo();
        fragment.hero = hero;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hero_stats, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        options = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.antimage_vert)
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader = ImageLoader.getInstance();
        updateForConfigChanged();

        initImage();

        showHeroInfo();
    }

    @Override
    public void onDestroy() {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof GifDrawable) {
            ((GifDrawable) drawable).recycle();
            imageView.setImageDrawable(null);
        }
        super.onDestroy();
    }

    private void initImage() {
        imageView = (GifImageView) getView().findViewById(R.id.imgHero);
        File externalFilesDir;
        String dir;
        externalFilesDir = Environment.getExternalStorageDirectory();
        if (externalFilesDir == null) {
            externalFilesDir = new File(getActivity().getFilesDir().getPath() + getActivity().getPackageName() + "/files");
        } else {
            externalFilesDir = new File(externalFilesDir, "/Android/data/" + getActivity().getPackageName() + "/files");
        }
        dir = externalFilesDir.getAbsolutePath();
        dir += File.separator + "anim" + File.separator;
        File gifFile = new File(dir + hero.getDotaId() + File.separator, "anim.gif");
        if (gifFile.exists()) {
            try {
                GifDrawable gifFromFile = new GifDrawable(gifFile);
                imageView.setImageDrawable(gifFromFile);
                ((ImageView) getView().findViewById(R.id.imgPortraitOverlay)).setImageResource(R.drawable.herogif_overlay);
            } catch (IOException e) {
                //ignored
            }
        } else {
            ((ImageView) getView().findViewById(R.id.imgPortraitOverlay)).setImageResource(R.drawable.heroprimaryportrait_overlay);
            imageLoader.displayImage("assets://heroes/" + hero.getDotaId() + "/vert.jpg",
                    imageView, options);
        }
    }

    @SuppressWarnings("deprecation")
    private void showHeroInfo() {
        HeroStats stats = hero.getStats();
        View fragmentView = getView();
        //((TextView)fragmentView.findViewById(R.id.title)).setText(hero.getLocalizedName());
        ((TextView) fragmentView.findViewById(R.id.txtFaction)).setText(stats.getAlignment() == 0 ? "Radiant" : "Dire");
        addHeroRoles(fragmentView);

        int bkgId;
        int redTextId;
        int level1baseAttr;
        int level16baseAttr;
        int level25baseAttr;
        switch (stats.getPrimaryStat()) {
            case 0:
                bkgId = R.id.str_bkg;
                redTextId = R.id.txtStrength;
                level1baseAttr = stats.getBaseStr();
                level16baseAttr = Math.round(stats.getBaseStr() + 15 * stats.getStrGain() + 2);
                level25baseAttr = Math.round(stats.getBaseStr() + 24 * stats.getStrGain() + 2 * 10);
                break;
            case 1:
                bkgId = R.id.agi_bkg;
                redTextId = R.id.txtAgility;
                level1baseAttr = stats.getBaseAgi();
                level16baseAttr = Math.round(stats.getBaseAgi() + 15 * stats.getAgiGain() + 2);
                level25baseAttr = Math.round(stats.getBaseAgi() + 24 * stats.getAgiGain() + 2 * 10);
                break;
            case 2:
            default:
                bkgId = R.id.int_bkg;
                redTextId = R.id.txtIntelligence;
                level1baseAttr = stats.getBaseInt();
                level16baseAttr = Math.round(stats.getBaseInt() + 15 * stats.getIntGain() + 2);
                level25baseAttr = Math.round(stats.getBaseInt() + 24 * stats.getIntGain() + 2 * 10);
        }
        /*Primal attribute*/
        fragmentView.findViewById(bkgId)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.overviewicon_primary));
        ((TextView) fragmentView.findViewById(redTextId)).setTextColor(Color.RED);
        /*Base Attributes*/
        ((TextView) fragmentView.findViewById(R.id.txtStrength))
                .setText(String.valueOf(stats.getBaseStr()) + "+" + String.valueOf(stats.getStrGain()));
        ((TextView) fragmentView.findViewById(R.id.txtAgility))
                .setText(String.valueOf(stats.getBaseAgi()) + "+" + String.valueOf(stats.getAgiGain()));
        ((TextView) fragmentView.findViewById(R.id.txtIntelligence))
                .setText(String.valueOf(stats.getBaseInt()) + "+" + String.valueOf(stats.getIntGain()));
		/*Hit points*/
        ((TextView) fragmentView.findViewById(R.id.health1))
                .setText(String.valueOf(Math.round(150 + 19/*hp per str*/ * stats.getBaseStr())));
        ((TextView) fragmentView.findViewById(R.id.health16)).setText(
                String.valueOf(Math.round(150 + 19 * ((float) stats.getBaseStr() + 2f + 15 * stats.getStrGain()))));
        ((TextView) fragmentView.findViewById(R.id.health25)).setText(
                String.valueOf(Math.round(150 + 19 * ((float) stats.getBaseStr() + 2f * 10 + 24 * stats.getStrGain()))));
		/*Mana points*/
        ((TextView) fragmentView.findViewById(R.id.mana1))
                .setText(String.valueOf(13/*mp per int*/ * stats.getBaseInt()));
        ((TextView) fragmentView.findViewById(R.id.mana16))
                .setText(String.valueOf(Math.round(13 * ((float) stats.getBaseInt() + 2f + 15 * stats.getIntGain()))));
        ((TextView) fragmentView.findViewById(R.id.mana25)).setText(
                String.valueOf(Math.round(13 * ((float) stats.getBaseInt() + 2f * 10 + 24 * stats.getIntGain()))));
		/*Damage*/
        //base attribute gives +1 to damage
        ((TextView) fragmentView.findViewById(R.id.damage1)).setText(stats.getMinDmg() + "-" + stats.getMaxDmg());
        ((TextView) fragmentView.findViewById(R.id.damage16)).setText(
                (stats.getMinDmg() + level16baseAttr - level1baseAttr) + "-" + (stats
                        .getMaxDmg() + level16baseAttr - level1baseAttr));
        ((TextView) fragmentView.findViewById(R.id.damage25)).setText(
                (stats.getMinDmg() + level25baseAttr - level1baseAttr) + "-" + (stats
                        .getMaxDmg() + level25baseAttr - level1baseAttr));
		/*Armor*/
        //each agi increase armor by 0.14. base agi doesn't count
        ((TextView) fragmentView.findViewById(R.id.armor1)).setText(String.valueOf(Math.round(stats.getArmor())));
        ((TextView) fragmentView.findViewById(R.id.armor16))
                .setText(String.valueOf(Math.round(stats.getArmor() + 0.14f * (stats.getAgiGain() * 15 + 2f))));
        ((TextView) fragmentView.findViewById(R.id.armor25))
                .setText(String.valueOf(Math.round(stats.getArmor() + 0.14f * (stats.getAgiGain() * 24 + 2f * 10))));
		/*Attacks per second=(1+ias)/base attack time..  -80<=ias<=400*/
        //each agi increase ias by 1
        int as = (int) ((100f + Math.min(Math.max(-80f, (float) stats.getBaseAgi()), 400f)) / stats.getBaseAttackTime());
        ((TextView) fragmentView.findViewById(R.id.attack_speed1)).setText(String.valueOf((float) as / 100));
        as = (int) ((100f + Math
                .min(Math.max(-80f, (float) stats.getBaseAgi() + (stats.getAgiGain() * 15 + 2f)), 400f)) / stats
                .getBaseAttackTime());
        ((TextView) fragmentView.findViewById(R.id.attack_speed16)).setText(String.valueOf((float) as / 100));
        as = (int) ((100f + Math
                .min(Math.max(-80f, (float) stats.getBaseAgi() + (stats.getAgiGain() * 24 + 2f * 10)), 400f)) / stats
                .getBaseAttackTime());
        ((TextView) fragmentView.findViewById(R.id.attack_speed25)).setText(String.valueOf((float) as / 100));
		/*Movement Speed*/
        ((TextView) fragmentView.findViewById(R.id.movement_speed1)).setText(String.valueOf(stats.getMovespeed()));
		/*Attack Range*/
        int range = stats.getRange();
        ((TextView) fragmentView.findViewById(R.id.attack_range1)).setText(
                range == 128 ? MessageFormat.format(getActivity().getString(R.string.melee), range) : String
                        .valueOf(range));
		/*Turn rate*/
        ((TextView) fragmentView.findViewById(R.id.turn_rate1)).setText(String.valueOf(stats.getTurnrate()));
		/*Sight range*/
        ((TextView) fragmentView.findViewById(R.id.sight_range1))
                .setText(stats.getDayVision() + "/" + stats.getNightVision());
		/*Attack duration*/
        ((TextView) fragmentView.findViewById(R.id.attack_duration1))
                .setText(stats.getAttackPoint() + "+" + stats.getAttackSwing());
		/*Cast duration*/
        ((TextView) fragmentView.findViewById(R.id.cast_duration1))
                .setText(stats.getCastPoint() + "+" + stats.getCastSwing());
		/*Missile sped*/
        ((TextView) fragmentView.findViewById(R.id.missile_speed1)).setText(
                stats.getProjectileSpeed() != 0 ? String.valueOf(stats.getProjectileSpeed()) : getActivity()
                        .getString(R.string.instant));
        String locale = getString(R.string.language);
        String path = "heroes/" + hero.getDotaId() + "/lore_" + locale + ".txt";
        ((TextView) fragmentView.findViewById(R.id.txtLore)).setText(Html.fromHtml(FileUtils.getTextFromAsset(getActivity(), path)));
        //http://www.playdota.com/mechanics/damagearmor
    }

    private void addHeroRoles(View root) {
        LinearLayout rolesHolder = (LinearLayout) root.findViewById(R.id.roles_holder);
        rolesHolder.removeAllViews();
        HeroStats heroStat = hero.getStats();
        if (heroStat.getRoles() != null) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            for (String role : heroStat.getRoles()) {
                TextView textView = new TextView(getActivity());
                textView.setLayoutParams(layoutParams);
                textView.setText(ResourceUtils.getLocalizedHeroRole(getActivity(), role));
                int drawableId = ResourceUtils.getRoleDrawable(role);

                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setCompoundDrawablePadding(Utils.dpSize(getActivity(), 5));
                textView.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
                rolesHolder.addView(textView);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateForConfigChanged();
    }

    private void updateForConfigChanged() {
        LinearLayout portraitHolder = (LinearLayout) getView().findViewById(R.id.portrait_holder1);
        LinearLayout mainHolder = (LinearLayout) getView().findViewById(R.id.main_vertical);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainHolder.setOrientation(LinearLayout.HORIZONTAL);
            portraitHolder.setOrientation(LinearLayout.VERTICAL);
        } else {
            mainHolder.setOrientation(LinearLayout.VERTICAL);
            portraitHolder.setOrientation(LinearLayout.HORIZONTAL);
        }
    }
}
