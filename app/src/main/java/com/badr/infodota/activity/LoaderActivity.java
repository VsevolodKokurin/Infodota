package com.badr.infodota.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.badr.infodota.BeanContainer;
import com.badr.infodota.R;
import com.badr.infodota.api.Constants;
import com.badr.infodota.api.abilities.Ability;
import com.badr.infodota.api.abilities.AbilityResult;
import com.badr.infodota.api.heroes.GetHeroes;
import com.badr.infodota.api.heroes.GetHeroesSkills;
import com.badr.infodota.api.heroes.Hero;
import com.badr.infodota.api.heroes.Skill;
import com.badr.infodota.api.items.GetItems;
import com.badr.infodota.api.items.Item;
import com.badr.infodota.api.items.ItemTypes;
import com.badr.infodota.api.responses.HeroResponse;
import com.badr.infodota.api.responses.HeroResponsesResult;
import com.badr.infodota.dao.Helper;
import com.badr.infodota.service.LocalUpdateService;
import com.badr.infodota.service.hero.HeroService;
import com.badr.infodota.service.item.ItemService;
import com.badr.infodota.util.FileUtils;
import com.badr.infodota.util.LoaderProgressTask;
import com.badr.infodota.util.ProgressTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.L;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * User: ABadretdinov
 * Date: 15.01.14
 * Time: 11:58
 */
public class LoaderActivity extends Activity {
    private static final int PLAY_SERVICES_REQUEST = 1001;
    TextView info;
    LocalUpdateService localUpdateService = BeanContainer.getInstance().getLocalUpdateService();
    private boolean showDialog = false;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader);
        L.writeLogs(false);
        SharedPreferences localPrefs = getSharedPreferences("locale", MODE_PRIVATE);
        String loc = localPrefs.getString("current", null);
        if (loc != null) {
            Locale locale = new Locale(loc);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getApplicationContext().getResources().updateConfiguration(config, null);
        }
        //progressBar= (ProgressBar)findViewById(R.id.progressBar);
        info = (TextView) findViewById(R.id.info);

        final int currentVersion = localUpdateService.getVersion(this);
        if (currentVersion != Helper.DATABASE_VERSION) {
            new LoaderProgressTask<String>(new ProgressTask<String>() {
                @Override
                public String doTask(OnPublishProgressListener listener) throws Exception {
                    ImageLoader.getInstance().clearDiskCache();
                    ImageLoader.getInstance().clearMemoryCache();
                    AssetManager assetManager = LoaderActivity.this.getAssets();
                    String[] files = assetManager.list("updates");
                    for (String fileName : files) {
                        int fileVersion = Integer.valueOf(fileName.split("\\.")[0]);
                        /*if (fileVersion > currentVersion) {*///пока что мы все скрипты прогоняем
                            String sql = FileUtils.getTextFromAsset(LoaderActivity.this, "updates" + File.separator + fileName);
                            localUpdateService.update(LoaderActivity.this, sql, fileVersion);
                        /*}*/
                    }
                    return "";
                }

                @Override
                public void doAfterTask(String result) {
                    localUpdateService.setUpdated(LoaderActivity.this);
                    showDialog = true;
                    checkGooglePlayServicesAndRun();
                }

                @Override
                public void handleError(String error) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoaderActivity.this);
                    dialog.setTitle(getString(R.string.error_during_load));
                    dialog.setMessage(error);
                    dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                }

                @Override
                public String getName() {
                    return null;
                }
            }, null).execute();
            //todo это на инициализацию полностью перевесить new HeroesLoader().execute();
        } else {
            showDialog = true;
            checkGooglePlayServicesAndRun();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showDialog) {
            checkGooglePlayServicesAndRun();
        }
    }

    private void runApp() {
        startActivity(new Intent(this, ListHolderActivity.class));
        finish();
    }

    private void checkGooglePlayServicesAndRun() {
        /*int code= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(ConnectionResult.SUCCESS==code){*/
        runApp();
        /*}
        else {
            //showDialog=false;
            GooglePlayServicesUtil.getErrorDialog(code, this,PLAY_SERVICES_REQUEST,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    finish();
                }
            }).show();
			info.setText(getString(R.string.loading_heroes_completed_and_stoped));
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLAY_SERVICES_REQUEST) {
            showDialog = true;
            /*if(resultCode==RESULT_CANCELED)
            {
                checkGooglePlayServicesAndRun();
            }*/
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_toast), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;

            }
        }, 2000);
    }

    public class HeroesLoader extends AsyncTask<String, String, String> {
        public static final String SUCCESS_CODE = "success";
        private DefaultHttpClient client;

        @Override
        protected String doInBackground(String... params) {
            try {
                if (Constants.INITIALIZATION) {
                    String[] supportedLanguages = new String[]{"russian", "english"};
                    for (String locale : supportedLanguages) {
                        saveItemForLocale(locale);
                    }
                } else {
                    saveItemForLocale("russian");
                }
                //reorganizeGuides();

                String heroesEntity = null;
                if (Constants.INITIALIZATION) {
                    client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(Constants.Heroes.SUBURL + getString(R.string.api));
                    HttpResponse response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(LoaderActivity.this, "Не удалось получить список героев",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    }
                    heroesEntity = EntityUtils.toString(response.getEntity());
                    saveStringFile("heroes.json", heroesEntity);
                } else {
                    heroesEntity = FileUtils.getTextFromAsset(LoaderActivity.this, "heroes.json");
                }
                // publishProgress("грузим список героев");
                GetHeroes getHeroes = new Gson().fromJson(heroesEntity, GetHeroes.class);
                List<Hero> heroes = getHeroes.getResult().getHeroes();
                //publishProgress(getString(R.string.heroes_list),String.valueOf(size),String.valueOf(0));

                // publishProgress("грузим скилы");
                String abilitiesJson = FileUtils.getTextFromAsset(LoaderActivity.this, "npc_abilities.json");
                AbilityResult abilityResult = new Gson().fromJson(abilitiesJson, AbilityResult.class);
                /*List<Ability> abilitiesList=abilityResult.getAbilities();
                Collections.sort(abilitiesList);
				abilityResult.setAbilities(abilitiesList);
				saveStringFile("npc_abilities.json",new Gson().toJson(abilityResult));
				*/
                HeroService heroService = BeanContainer.getInstance().getHeroService();
                for (int heroesLoaded = 0; heroesLoaded < heroes.size(); heroesLoaded++) {
                    Hero hero = heroes.get(heroesLoaded);
                    //publishProgress(hero.getLocalizedName(),String.valueOf(position));
                    // publishProgress("сохраняем героя:"+hero.getLocalizedName());

                    heroService.saveHero(LoaderActivity.this, hero);
                    // publishProgress("грузим статы героя:"+hero.getLocalizedName());
                    //loadHeroStats(hero);
                    //publishProgress("грузим скилы героя:"+hero.getLocalizedName());
                    loadHeroAbilities(hero, abilityResult);
                    // publishProgress("загружено для:"+hero.getLocalizedName());
                    //only for initial loading
                    //loadHeroLore(hero);
                    //loadHeroSkillsYouTube(hero);
                    //loadPicture(hero, 0);
                    //loadPicture(hero,1);
                    //loadPicture(hero,2);
                    //loadPicture(hero,3);
                    //reorganizeHeroGuides(hero);
                }
                //todo сперва необходимо создать всех героев, чтобы responses были нормальными
				/*for(Hero hero:heroes){
					loadHeroResponses(hero);
				}*/

                List<Ability> abilities = abilityResult.getAbilities();
                if (abilities.size() > 0) {
                    for (Ability ability : abilities) {
                        if (ability.getName() != null) {
                            publishProgress("сохраняем скил:" + ability.getName());
                        }
                        ability.setHeroId(0);
                        heroService.saveAbility(LoaderActivity.this, ability);
                    }
                }
                //only for initial loading
                //getSkills("russian");
                //getSkills("english");
                return SUCCESS_CODE;
            } catch (IOException e) {
                e.printStackTrace();  //
                return e.getLocalizedMessage();
            }
        }

        private void reorganizeHeroGuides(Hero hero) {
            File dota = new File(Environment.getExternalStorageDirectory(), "dota");
            File heroGuidesFile = new File(dota, hero.getDotaId());
            heroGuidesFile.mkdirs();
            if (dota.exists() && dota.isDirectory()) {
                File[] builds = dota.listFiles();
                for (File build : builds) {
                    if (build.isFile()) {
                        if (build.getName().contains(hero.getDotaId())) {
                            String buildString = FileUtils.getTextFromFile(build.getPath());
                            saveStringFile(heroGuidesFile.getName() + "/" + build.getName().replace(hero.getDotaId() + "_", ""), buildString);
                            build.delete();
                        }
                       /* else {
                            String buildString=Utils.getTextFromFile(build.getPath());
                            if(buildString.contains("\"Hero\"\t\""+hero.getDotaId()+"\"")){
                                saveStringFile(heroGuidesFile.getName()+"/"+build.getName(),buildString);
                                build.delete();
                            }
                        }*/
                    }
                }
            }

        }

        private void reorganizeGuides() {
            // /workshop/folder/lone_druid_2313231.build
            // put workshop folder into sdcard
            File workshop = new File(Environment.getExternalStorageDirectory(), "workshop");
            if (workshop.exists() && workshop.isDirectory()) {
                for (File folder : workshop.listFiles()) {
                    if (folder.isDirectory()) {
                        for (File build : folder.listFiles()) {
                            String fileName = build.getName();
                            if (fileName.endsWith(".build")) {
                                String buildString = FileUtils.getTextFromFile(build.getPath());
                                saveStringFile(fileName, buildString);
                                build.delete();
                            }
                        }
                        if (folder.list().length == 0) {
                            folder.delete();
                        }
                    }
                }
                if (workshop.list().length == 0) {
                    workshop.delete();
                }
            }
        }

        private void loadHeroAbilities(Hero hero, AbilityResult abilityResult) {
            String skillsJson = FileUtils.getTextFromAsset(LoaderActivity.this, "heroes/" + hero.getDotaId() + "/skills_russian.json");
            Type skillListType = new TypeToken<List<Skill>>() {
            }.getType();
            final List<Skill> skills = new Gson().fromJson(skillsJson, skillListType);

            HeroService heroService = BeanContainer.getInstance().getHeroService();
            List<Ability> abilities = abilityResult.getAbilities();
            for (Skill skill : skills) {
                int i = 0;
                boolean found = false;
                while (i < abilities.size() && !found) {
                    Ability ability = abilities.get(i);
                    if (ability.getName().equals(skill.getName())) {
                        found = true;
                        ability.setHeroId(hero.getId());
                        heroService.saveAbility(LoaderActivity.this, ability);
                        abilities.remove(ability);
                    } else {
                        i++;
                    }
                }
            }
        }

        private void saveItemForLocale(String locale) {
            String itemsEntity = FileUtils.getTextFromAsset(LoaderActivity.this, "items_" + locale + ".json");
            GetItems getItems = new Gson().fromJson(itemsEntity, GetItems.class);
            Map<String, Item> itemMap = getItems.getItemdata();
            Set<String> itemDotaIds = itemMap.keySet();
            List<Item> itemList = new ArrayList<Item>();
            String itemTypesEntity = FileUtils.getTextFromAsset(LoaderActivity.this, "item_types.json");
            ItemTypes itemTypes = new Gson().fromJson(itemTypesEntity, ItemTypes.class);
            //todo сделать Map<String,List<String>>. и удалять, когда находим.
            ItemService itemService = BeanContainer.getInstance().getItemService();
            //первый проход. сохраняем все предметы
            for (String itemDotaId : itemDotaIds) {
                // в бд храним только id, dotaId, dname. остальное раскидать по файлам dotaId_locale.json
                Item item = itemMap.get(itemDotaId);
                item.setDotaId(itemDotaId);

                //ищем itemType
                boolean foundType = false;
                for (int i = 0; !foundType && i < itemTypes.getArcane().size(); i++) {
                    String itemName = itemTypes.getArcane().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("arcane");
                        itemTypes.getArcane().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getArmaments().size(); i++) {
                    String itemName = itemTypes.getArmaments().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("armaments");
                        itemTypes.getArmaments().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getArmor().size(); i++) {
                    String itemName = itemTypes.getArmor().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("armor");
                        itemTypes.getArmor().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getArtifacts().size(); i++) {
                    String itemName = itemTypes.getArtifacts().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("artifacts");
                        itemTypes.getArtifacts().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getAttributes().size(); i++) {
                    String itemName = itemTypes.getAttributes().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("attributes");
                        itemTypes.getAttributes().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getCaster().size(); i++) {
                    String itemName = itemTypes.getCaster().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("caster");
                        itemTypes.getCaster().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getCommon().size(); i++) {
                    String itemName = itemTypes.getCommon().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("common");
                        itemTypes.getCommon().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getConsumable().size(); i++) {
                    String itemName = itemTypes.getConsumable().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("consumable");
                        itemTypes.getConsumable().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getSupport().size(); i++) {
                    String itemName = itemTypes.getSupport().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("support");
                        itemTypes.getSupport().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getSecret_shop().size(); i++) {
                    String itemName = itemTypes.getSecret_shop().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("secret_shop");
                        itemTypes.getSecret_shop().remove(i);
                    }
                }
                for (int i = 0; !foundType && i < itemTypes.getWeapons().size(); i++) {
                    String itemName = itemTypes.getWeapons().get(i);
                    if (itemName.equals(itemDotaId)) {
                        foundType = true;
                        item.setType("weapons");
                        itemTypes.getWeapons().remove(i);
                    }
                }
                itemService.saveItem(LoaderActivity.this, item);
                if (Constants.INITIALIZATION) {
                    saveStringFile("items/" + itemDotaId + "_" + locale + ".json", new Gson().toJson(item));
                }
                itemList.add(item);
            }
            //второй проход - сохраняем все рецепты
            if ("russian".equals(locale)) {

                itemService.saveFromToItems(LoaderActivity.this, itemList);
            }
        }
/*
        private void loadHeroStats(Hero hero) throws IOException
		{
			String entity=null;
			String name=hero.getLocalizedName();
			if(Constants.INITIALIZATION){
				// UrlEncode.encode(name,"utf-8") makes Crystal+Maiden from Crystal Maiden instead of Crystal%20Maiden, which we need to get
				String statsUrl=Constants.Heroes.STATS_URL+("Io".equals(name)?"Wisp":"Windranger".equals(name)?"Windrunner":"Necrophos".equals(name)?"Necrolyte":"Lycan".equals(name)?"Lycanthrope":"Wraith King".equals(name)?"Skeleton%20King": name.replace(" ","%20"));
				//System.out.println(name+": "+statsUrl);
				HttpResponse response=client.execute(new HttpGet(statsUrl));
				if(response.getStatusLine().getStatusCode()==200)
				{
					try
					{
						entity=EntityUtils.toString(response.getEntity());
						saveStringFile("heroes/"+hero.getDotaId()+"/stats.json",entity);
					}
					catch (Exception e)
					{
						Log.e(LoaderActivity.class.getName(),name + " stats loading failure");
					}
				}
			}
			else {
				entity=FileUtils.getTextFromAsset(LoaderActivity.this,"heroes/"+hero.getDotaId()+"/stats.json");
			}
			if(entity!=null)
			{
				HeroStats stats=new Gson().fromJson(entity,HeroStats.class);
				dao.createHeroStats(hero.getId(),stats);
			}
		}*/

        private void saveStringFile(String fileName, String data) {
            //todo
            File skillFile = new File(Environment.getExternalStorageDirectory().getPath() + "/dota/" + fileName);
            skillFile.getParentFile().mkdirs();
            try {
                FileOutputStream fOut = new FileOutputStream(skillFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(data);
                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void loadPicture(Hero hero, int imageState) throws IOException {
            String pictureUrl;
            String localPath = Environment.getExternalStorageDirectory().getPath() + "/dota/";
            switch (imageState) {
                case 0:
                    pictureUrl = MessageFormat.format(Constants.Heroes.FULL_IMAGE_URL, hero.getDotaId());
                    localPath += hero.getDotaId() + "/full.png";
                    break;
                case 1:
                    pictureUrl = MessageFormat.format(Constants.Heroes.SB_IMAGE_URL, hero.getDotaId());
                    localPath += "_sb.png";
                    break;
                case 2:
                    pictureUrl = MessageFormat.format(Constants.Heroes.VERT_IMAGE_URL, hero.getDotaId());
                    localPath += hero.getDotaId() + "/vert.jpg";
                    break;
                default:
                    pictureUrl = MessageFormat.format(Constants.Heroes.MINIMAP_IMAGE_URL, hero.getLocalizedName().replace(" ", "_"));
                    localPath += "_minimap.png";
            }
            File localFile = new File(localPath);
            localFile.getParentFile().mkdirs();
            if (!localFile.exists()) {
                HttpResponse heroPicture = client.execute(new HttpGet(pictureUrl));
                //System.out.println(hero.getLocalizedName()+": "+pictureUrl);

                if (heroPicture.getStatusLine().getStatusCode() == 200) {
                    HttpEntity pictureEntity = heroPicture.getEntity();
                    int count;
                    InputStream input = new BufferedInputStream(pictureEntity.getContent());
                    OutputStream output = new FileOutputStream(localPath);

                    byte data[] = new byte[1024];
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                } else if (imageState > 2) {
                    String name = hero.getDotaId();
                    String[] regions = name.split("_");
                    StringBuffer realName = new StringBuffer();
                    for (String region : regions) {
                        realName.append(region.substring(0, 1).toUpperCase());
                        realName.append(region.substring(1));
                        realName.append("_");
                    }
                    realName.deleteCharAt(realName.length() - 1);
                    pictureUrl = MessageFormat.format(Constants.Heroes.MINIMAP_IMAGE_URL, realName);
                    heroPicture = client.execute(new HttpGet(pictureUrl));
                    //System.out.println(hero.getLocalizedName()+": "+pictureUrl);

                    if (heroPicture.getStatusLine().getStatusCode() == 200) {
                        HttpEntity pictureEntity = heroPicture.getEntity();
                        int count;
                        InputStream input = new BufferedInputStream(pictureEntity.getContent());
                        OutputStream output = new FileOutputStream(localPath);

                        byte data[] = new byte[1024];
                        while ((count = input.read(data)) != -1) {
                            output.write(data, 0, count);
                        }
                        output.flush();
                        output.close();
                        input.close();
                    }
                }
            }
        }

        /*
        *
        * heroes/
        *   abbadon/
        *   antimage/
        *       full.png
        *       mini.png
        *       vert.jpg
        *       lore_ru.txt
        *       lore_en.txt
        *       skills/
        *           1.antimage_blink_hp1.png
        *           1.antimage_blink.txt
         */
        public void getSkills(String fileName) {
            String allSkills = FileUtils.getTextFromAsset(LoaderActivity.this, fileName + ".json");
            GetHeroesSkills result = new Gson().fromJson(allSkills, GetHeroesSkills.class);
            Map<String, Skill> skillMap = result.getAbilitydata();
            Map<String, List<Skill>> heroSkills = new HashMap<String, List<Skill>>();
            Set<String> skillSet = skillMap.keySet();
            for (String skill : skillSet) {
                Skill tekSkill = skillMap.get(skill);
                tekSkill.setName(skill);
                String heroName = tekSkill.getHurl().replace("_", " ");
                if ("Io".equals(heroName)) {
                    heroName = "Wisp";
                } else if ("Natures Prophet".equals(heroName)) {
                    heroName = "Nature's Prophet";
                }
                HeroService heroService = BeanContainer.getInstance().getHeroService();
                List<Hero> heroes = heroService.getHeroesByName(LoaderActivity.this, heroName);
                Hero hero = heroes.size() != 0 ? heroes.get(0) : null;
                if (hero != null) {
                    String name = hero.getDotaId();
                    if (heroSkills.containsKey(name)) {
                        List<Skill> skills = heroSkills.get(name);
                        tekSkill.setId(skills.size());
                        skills.add(tekSkill);
                    } else {
                        List<Skill> skills = new ArrayList<Skill>();
                        tekSkill.setId(0);
                        skills.add(tekSkill);
                        heroSkills.put(name, skills);
                    }
                }
            }
            for (String hero : heroSkills.keySet()) {
                String json = new Gson().toJson(heroSkills.get(hero));
                saveStringFile(hero + "/skills_" + fileName + ".json", json);
            }
        }

        //http://www.dota2.com/hero/Anti-Mage
        //div id="bioInner"
        public void loadHeroLore(Hero hero) throws IOException {
            String locale = getString(R.string.language);
            //' - Nature's Prophet fix
            String url = MessageFormat.format(Constants.Heroes.DOTA2_HEROPEDIA_URL, hero.getLocalizedName().replace(" ", "_").replace("'", ""), locale);
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("div[id=bioInner]");
            if (elements != null && elements.size() != 0) {
                String lore = elements.get(0).html();
                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/dota/" + hero.getDotaId() + "/" + locale + ".txt");
                file.getParentFile().mkdirs();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(lore);
                myOutWriter.close();
                fOut.close();
            }
        }

        public void loadHeroSkillsYouTube(Hero hero) throws IOException {
            String locale = getString(R.string.language);
            //' - Nature's Prophet fix
            String url = MessageFormat.format(Constants.Heroes.DOTA2_HEROPEDIA_URL, hero.getLocalizedName().replace(" ", "_").replace("'", ""), locale);
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("div[class=abilitiesInsetBoxInner]");
            System.out.println();
            System.out.println();
            System.out.println(hero.getDotaId());
            if (elements != null && elements.size() != 0) {
                for (Element abilityElement : elements) {
                    Elements abilityHeader = abilityElement.select("div[class=abilityHeaderRowDescription]");
                    String header = null;
                    if (abilityHeader != null && abilityHeader.size() > 0) {
                        Element headerElement = abilityHeader.select("h2").first();
                        header = headerElement.html();
                    }
                    Elements youtubeHolder = abilityElement.select("div[class=abilityVideoContainer]");
                    String youTubeSrc = null;
                    if (youtubeHolder != null && youtubeHolder.size() != 0) {
                        Element youTubeElement = youtubeHolder.first().select("iframe").first();
                        youTubeSrc = youTubeElement.attr("src");
                        youTubeSrc = youTubeSrc.replace("http://www.youtube.com/embed/", "");
                        youTubeSrc = youTubeSrc.replace("?hd=1&rel=0", "");
                    }
                    System.out.println(header + " " + (youTubeSrc != null ? youTubeSrc : ""));
                }
                System.out.println();
            }
        }

        private void loadHeroResponses(Hero hero) throws IOException {
            String heroName = hero.getLocalizedName().replace("'", "%27").replace(' ', '_');

            String url = MessageFormat.format(Constants.Heroes.DOTA2_WIKI_RESPONSES_URL, heroName);
            System.out.println("hero url: " + url);
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("a[title=Play]");
            HeroResponsesResult result = new HeroResponsesResult();
            result.setResponses(new ArrayList<HeroResponse>());

            HeroService heroService = BeanContainer.getInstance().getHeroService();
            if (elements != null && elements.size() != 0) {
                //System.out.println(elements);
                for (Element mp3element : elements) {
                    HeroResponse heroResponse = new HeroResponse();
                    Element parent = mp3element.parent();
                    Elements parentChildren = parent.children();
                    int size = parentChildren.size();
                    heroResponse.setOthers(new ArrayList<String>());
                    for (int i = 1; i < size; i++) {
                        String otherHeroName = parentChildren.get(i).attr("title");
                        List<Hero> heroesWithThisName = heroService.getHeroesByName(LoaderActivity.this, otherHeroName);
                        if (heroesWithThisName != null && heroesWithThisName.size() > 0) {
                            heroResponse.getOthers().add(heroesWithThisName.get(0).getDotaId());
                        } else {
                            heroResponse.getOthers().add(otherHeroName);
                        }
                        //System.out.println(otherHeroName);
                    }
                    String mp3Url = mp3element.attr("href");
                    heroResponse.setUrl(mp3Url);
                    String mp3Title = null;
                    List<TextNode> nodes = parent.textNodes();
                    if (nodes.size() > 0) {
                        mp3Title = nodes.get(nodes.size() - 1).toString();
                    }
                    heroResponse.setTitle(mp3Title);
                    ///System.out.println("mp3 title: "+mp3Title+"url:"+mp3Url);
                    result.getResponses().add(heroResponse);
                }
                elements.get(0);
            } else {
                System.out.println(url);
            }
            String responsesAsJson = new Gson().toJson(result);
            saveStringFile(hero.getDotaId() + "/responses.json", responsesAsJson);
            System.out.println("hero responses saving complete");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //info.setText(values[0]);

            if (values.length > 2) {
                //info.setText(values[0]);
                //progressBar.setMax(Integer.valueOf(values[1]));
                //progressBar.setProgress(Integer.valueOf(values[2]));
            } else {
                //info.setText(MessageFormat.format(getString(R.string.loading_data_for),values[0]));
                //progressBar.setProgress(Integer.valueOf(values[1]));
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (SUCCESS_CODE.equals(s)) {
                showDialog = true;
                checkGooglePlayServicesAndRun();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoaderActivity.this);
                dialog.setTitle(getString(R.string.error_during_load));
                dialog.setMessage(s);
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                dialog.show();
            }
            info.setText(getString(R.string.loading_heroes_completed_and_stoped));
        }
    }
}
