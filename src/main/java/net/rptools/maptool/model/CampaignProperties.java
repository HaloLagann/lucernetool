/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import com.google.protobuf.StringValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.token.*;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.server.proto.CampaignPropertiesDto;
import net.rptools.maptool.server.proto.LightSourceListDto;
import net.rptools.maptool.server.proto.TokenPropertyListDto;

public class CampaignProperties {
  public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "NPC";
  public static final String DEFAULT_PC_TOKEN_PROPERTY_TYPE = "Player Character";

  private Map<String, List<TokenProperty>> tokenTypeMap = new HashMap<>();
  private List<String> remoteRepositoryList = new ArrayList<>();
  private Map<String, Map<GUID, LightSource>> lightSourcesMap = new TreeMap<>();
  private Map<String, LookupTable> lookupTableMap = new HashMap<>();
  private Map<String, SightType> sightTypeMap = new HashMap<>();

  private String defaultSightType;

  private Map<String, BooleanTokenOverlay> tokenStates = new LinkedHashMap<>();
  private Map<String, BarTokenOverlay> tokenBars = new LinkedHashMap<>();
  private Map<String, String> characterSheets = new HashMap<>();

  /** Flag indicating that owners have special permissions */
  private boolean initiativeOwnerPermissions = AppPreferences.getInitOwnerPermissions();

  /** Flag indicating that owners can only move tokens when they have initiative */
  private boolean initiativeMovementLock = AppPreferences.getInitLockMovement();

  /** Whether the default initiative sort order is reversed */
  private boolean initiativeUseReverseSort = false;

  /** Whether the Next/Previous buttons are disabled on the Initiative Panel */
  private boolean initiativePanelButtonsDisabled = false;

  public CampaignProperties() {}

  public CampaignProperties(CampaignProperties properties) {
    for (Entry<String, List<TokenProperty>> entry : properties.tokenTypeMap.entrySet()) {
      List<TokenProperty> typeList = new ArrayList<>(properties.tokenTypeMap.get(entry.getKey()));

      tokenTypeMap.put(entry.getKey(), typeList);
    }
    remoteRepositoryList.addAll(properties.remoteRepositoryList);

    lookupTableMap.putAll(properties.lookupTableMap);
    defaultSightType = properties.defaultSightType;
    sightTypeMap.putAll(properties.sightTypeMap);
    // TODO: This doesn't feel right, should we deep copy, or does this do that automatically ?
    lightSourcesMap.putAll(properties.lightSourcesMap);

    for (BooleanTokenOverlay overlay : properties.tokenStates.values()) {
      overlay = (BooleanTokenOverlay) overlay.clone();
      tokenStates.put(overlay.getName(), overlay);
    } // endfor

    for (BarTokenOverlay overlay : properties.tokenBars.values()) {
      overlay = (BarTokenOverlay) overlay.clone();
      tokenBars.put(overlay.getName(), overlay);
    } // endfor

    initiativeOwnerPermissions = properties.initiativeOwnerPermissions;
    initiativeMovementLock = properties.initiativeMovementLock;
    initiativeUseReverseSort = properties.initiativeUseReverseSort;
    initiativePanelButtonsDisabled = properties.initiativePanelButtonsDisabled;

    for (String type : properties.characterSheets.keySet()) {
      characterSheets.put(type, properties.characterSheets.get(type));
    }
  }

  public void mergeInto(CampaignProperties properties) {
    // This will replace any dups
    properties.tokenTypeMap.putAll(tokenTypeMap);
    // Need to cull out dups
    for (String repo : properties.remoteRepositoryList) {
      if (!remoteRepositoryList.contains(repo)) {
        remoteRepositoryList.add(repo);
      }
    }
    properties.lightSourcesMap.putAll(lightSourcesMap);
    properties.lookupTableMap.putAll(lookupTableMap);
    properties.sightTypeMap.putAll(sightTypeMap);
    properties.tokenStates.putAll(tokenStates);
    properties.tokenBars.putAll(tokenBars);
  }

  public Map<String, List<TokenProperty>> getTokenTypeMap() {
    return tokenTypeMap;
  }

  public Map<String, SightType> getSightTypeMap() {
    return sightTypeMap;
  }

  public void setSightTypeMap(Map<String, SightType> map) {
    if (map != null) {
      sightTypeMap.clear();
      sightTypeMap.putAll(map);
    }
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
  public void setTokenTypeMap(Map<String, List<TokenProperty>> map) {
    tokenTypeMap.clear();
    tokenTypeMap.putAll(map);
  }

  public List<TokenProperty> getTokenPropertyList(String tokenType) {
    return getTokenTypeMap().get(tokenType);
  }

  public List<String> getRemoteRepositoryList() {
    return remoteRepositoryList;
  }

  public void setRemoteRepositoryList(List<String> list) {
    remoteRepositoryList.clear();
    remoteRepositoryList.addAll(list);
  }

  public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
    return lightSourcesMap;
  }

  public void setLightSourcesMap(Map<String, Map<GUID, LightSource>> map) {
    lightSourcesMap.clear();
    lightSourcesMap.putAll(map);
  }

  public Map<String, LookupTable> getLookupTableMap() {
    return lookupTableMap;
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
  public void setLookupTableMap(Map<String, LookupTable> map) {
    lookupTableMap.clear();
    lookupTableMap.putAll(map);
  }

  public Map<String, BooleanTokenOverlay> getTokenStatesMap() {
    return tokenStates;
  }

  public void setTokenStatesMap(Map<String, BooleanTokenOverlay> map) {
    tokenStates.clear();
    tokenStates.putAll(map);
  }

  public Map<String, BarTokenOverlay> getTokenBarsMap() {
    return tokenBars;
  }

  public void setTokenBarsMap(Map<String, BarTokenOverlay> map) {
    tokenBars.clear();
    tokenBars.putAll(map);
  }

  public void initDefaultProperties() {
    initLightSourcesMap();
    initTokenTypeMap();
    initSightTypeMap();
    initTokenStatesMap();
    initTokenBarsMap();
    initCharacterSheetsMap();
  }

  private void initLightSourcesMap() {
    if (!lightSourcesMap.isEmpty()) {
      return;
    }

    try {
      Map<String, List<LightSource>> map = LightSource.getDefaultLightSources();
      for (var entry : map.entrySet()) {
        String key = entry.getKey();
        Map<GUID, LightSource> lightSourceMap = new LinkedHashMap<>();
        for (LightSource source : entry.getValue()) {
          lightSourceMap.put(source.getId(), source);
        }
        lightSourcesMap.put(key, lightSourceMap);
      }
    } catch (IOException ioe) {
      MapTool.showError("CampaignProperties.error.initLightSources", ioe);
    }
  }

  public String getDefaultSightType() {
    return defaultSightType;
  }

  // @formatter:off
  private static final Object[][] starter =
      new Object[][] {
        // Sight Type Name					Dist		Mult		Arc		LtSrc		Shape				Scale
        {"Normal", 0.0, 1.0, 0, null, null, false},
        {"Lowlight", 0.0, 2.0, 0, null, null, false},
        {"Grid Vision", 0.0, 1.0, 0, null, ShapeType.GRID, true},
        {"Square Vision", 0.0, 1.0, 0, null, ShapeType.SQUARE, false},
        {"Normal Vision - Short Range", 10.0, 1.0, 0, null, ShapeType.CIRCLE, true},
        {"Conic Vision", 0.0, 1.0, 120, null, ShapeType.CONE, false},
        {"Darkvision", 0.0, 1.0, 0, null, null, true},
      };
  // @formatter:on

  private void initSightTypeMap() {
    sightTypeMap.clear();
    for (Object[] row : starter) {
      SightType st =
          new SightType(
              (String) row[0],
              (Double) row[2],
              (LightSource) row[4],
              (ShapeType) row[5],
              (Integer) row[3],
              (boolean) row[6]);
      st.setDistance(((Double) row[1]).floatValue());
      sightTypeMap.put((String) row[0], st);
    }
    SightType dv = sightTypeMap.get("Darkvision");
    try {
      dv.setPersonalLightSource(LightSource.getDefaultLightSources().get("Generic").get(5));
      // sightTypeMap.put("Darkvision & Lowlight", new SightType("Darkvision", 2,
      // LightSource.getDefaultLightSources().get("Generic").get(4)));
    } catch (IOException e) {
      MapTool.showError("CampaignProperties.error.noGenericLight", e);
    }
    defaultSightType = (String) starter[0][0];
  }

  private void initTokenTypeMap() {
    if (!tokenTypeMap.isEmpty()) {
      return;
    }

    // NPC Token Properties
    List<TokenProperty> list = new ArrayList<>();
    List<TokenProperty> pcList = new ArrayList<>();
    list.add(new TokenProperty("Level", "0", true));
    list.add(new TokenProperty("PassivePerception", null, true, true, true, "10"));
    //
    list.add(new TokenProperty("------------Vitality---------", "-------"));
    list.add(new TokenProperty("TempHP", "0"));
    list.add(new TokenProperty("MaxHP", "17"));
    list.add(new TokenProperty("CurHP", "17"));
    list.add(new TokenProperty("minDeathDC", "8"));
    //
    list.add(new TokenProperty("--------Combat----------", "--------"));
    list.add(new TokenProperty("AC", "10", true, true, true));
    list.add(new TokenProperty("critThresh", "20"));
    list.add(new TokenProperty("Elevation", "0"));
    list.add(new TokenProperty("Movement", null, true, true, true, "30"));
    //
    list.add(new TokenProperty("-------Stat-Block--------", "---------"));
    list.add(new TokenProperty("STR", "10"));
    list.add(new TokenProperty("DEX", "10"));
    list.add(new TokenProperty("CON", "10"));
    list.add(new TokenProperty("ARC", "10"));
    list.add(new TokenProperty("DIV", "10"));
    list.add(new TokenProperty("OCU", "10"));
    list.add(new TokenProperty("INT", "10"));
    list.add(new TokenProperty("WIS", "10"));
    list.add(new TokenProperty("CHA", "10"));
    //
    list.add(new TokenProperty("--------SAVING THROW BONUSES---", "----------"));
    list.add(new TokenProperty("STRbonus", "0"));
    list.add(new TokenProperty("DEXbonus", "0"));
    list.add(new TokenProperty("CONbonus", "0"));
    list.add(new TokenProperty("ARCbonus", "0"));
    list.add(new TokenProperty("DIVbonus", "0"));
    list.add(new TokenProperty("OCUbonus", "0"));
    list.add(new TokenProperty("INTbonus", "0"));
    list.add(new TokenProperty("WISbonus", "0"));
    list.add(new TokenProperty("CHAbonus", "0"));
    //
    list.add(new TokenProperty("--------------------Skill Check Bonuses----"));
    list.add(new TokenProperty("Athletics", "0"));
    list.add(new TokenProperty("Intimidation", "0"));
    list.add(new TokenProperty("Acrobatics", "0"));
    list.add(new TokenProperty("Stealth", "0"));
    list.add(new TokenProperty("SleightOfHand", "0"));
    list.add(new TokenProperty("Endurance", "0"));
    list.add(new TokenProperty("Arcana", "0"));
    list.add(new TokenProperty("Magecraft", "0"));
    list.add(new TokenProperty("Religion", "0"));
    list.add(new TokenProperty("Theurgy", "0"));
    list.add(new TokenProperty("Obscura", "0"));
    list.add(new TokenProperty("Malis", "0"));
    list.add(new TokenProperty("History", "0"));
    list.add(new TokenProperty("Memory", "0"));
    list.add(new TokenProperty("Medicine", "0"));
    list.add(new TokenProperty("Investigation", "0"));
    list.add(new TokenProperty("Perception", "0"));
    list.add(new TokenProperty("Insight", "0"));
    list.add(new TokenProperty("Survival", "0"));
    list.add(new TokenProperty("Nature", "0"));
    list.add(new TokenProperty("Persuasion", "0"));
    list.add(new TokenProperty("Deception", "0"));
    list.add(new TokenProperty("Coercion", "0"));
    list.add(new TokenProperty("Performance", "0"));
    list.add(new TokenProperty("Artistry", "0"));
    //
    list.add(new TokenProperty("--------------------DONT-TOUCH---", "----------"));
    list.add(new TokenProperty("STRmod", "[r:floor((STR-10)/2)]]"));
    list.add(new TokenProperty("DEXmod", "[r:floor((DEX-10)/2)]]"));
    list.add(new TokenProperty("CONmod", "[r:floor((CON-10)/2)]]"));
    list.add(new TokenProperty("ARCmod", "[r:floor((ARC-10)/2)]]"));
    list.add(new TokenProperty("DIVmod", "[r:floor((DIV-10)/2)]]"));
    list.add(new TokenProperty("OCUmod", "[r:floor((OCU-10)/2)]]"));
    list.add(new TokenProperty("INTmod", "[r:floor((INT-10)/2)]]"));
    list.add(new TokenProperty("WISmod", "[r:floor((WIS-10)/2)]]"));
    list.add(new TokenProperty("CHAmod", "[r:floor((CHA-10)/2)]]"));
    list.add(new TokenProperty("curMaxHP", "[r:maxHP-woundedHP]"));
    list.add(new TokenProperty("woundedHP", "0"));
    list.add(
        new TokenProperty(
            "Vitality",
            null,
            true,
            true,
            true,
            "[r:CurHP] / [r:curMaxHP] [r:if(TempHP>0,\"| Ward: \"+TempHP,\"\")]]"));
    list.add(new TokenProperty("deathDC", "8"));
    list.add(
        new TokenProperty("Passive Perception", null, true, true, true, "[r:PassivePerception]"));
    list.add(new TokenProperty("wardMax", "0"));
    list.add(new TokenProperty("Torpor", "0"));

    tokenTypeMap.put(DEFAULT_TOKEN_PROPERTY_TYPE, list);

    // Player Character Token Properties
    pcList.add(new TokenProperty("*Class Level", "[r:ClassLevel]"));
    pcList.add(new TokenProperty("*Elevation", ""));
    pcList.add(new TokenProperty("*@Movement", "30"));
    pcList.add(new TokenProperty("STR", "10"));
    pcList.add(new TokenProperty("DEX", "10"));
    pcList.add(new TokenProperty("CON", "10"));
    pcList.add(new TokenProperty("ARC", "10"));
    pcList.add(new TokenProperty("DIV", "10"));
    pcList.add(new TokenProperty("OCU", "10"));
    pcList.add(new TokenProperty("INT", "10"));
    pcList.add(new TokenProperty("WIS", "10"));
    pcList.add(new TokenProperty("CHA", "10"));
    pcList.add(new TokenProperty("UsedSlots", "0"));
    pcList.add(new TokenProperty("BonusSlots", "0"));
    pcList.add(new TokenProperty("BonusVITdice", "0"));
    //
    pcList.add(new TokenProperty("-------------------Combat Information---:----------"));
    pcList.add(new TokenProperty("*@AC:10"));
    pcList.add(new TokenProperty("TempHP:0"));
    pcList.add(new TokenProperty("MaxHP:17"));
    pcList.add(new TokenProperty("minDeathDC:8"));
    pcList.add(new TokenProperty("critThresh:20"));
    //
    pcList.add(new TokenProperty("--------------------Character Classes---:----------"));
    pcList.add(new TokenProperty("Warrior:0"));
    pcList.add(new TokenProperty("Barbarian", "0"));
    pcList.add(new TokenProperty("Knight", "0"));
    pcList.add(new TokenProperty("Thief", "0"));
    pcList.add(new TokenProperty("Shadow", "0"));
    pcList.add(new TokenProperty("Ranger", "0"));
    pcList.add(new TokenProperty("Bard", "0"));
    pcList.add(new TokenProperty("Wizard", "0"));
    pcList.add(new TokenProperty("Sorcerer", "0"));
    pcList.add(new TokenProperty("Warlock", "0"));
    pcList.add(new TokenProperty("Druid", "0"));
    pcList.add(new TokenProperty("Inquisitor", "0"));
    pcList.add(new TokenProperty("Cleric", "0"));
    pcList.add(new TokenProperty("Paladin", "0"));
    //
    pcList.add(new TokenProperty("--------------------Skill Proficiency Toggles----"));
    pcList.add(new TokenProperty("Athletics", "0"));
    pcList.add(new TokenProperty("Intimidation", "0"));
    pcList.add(new TokenProperty("Acrobatics", "0"));
    pcList.add(new TokenProperty("Stealth", "0"));
    pcList.add(new TokenProperty("SleightOfHand", "0"));
    pcList.add(new TokenProperty("Endurance", "0"));
    pcList.add(new TokenProperty("Arcana", "0"));
    pcList.add(new TokenProperty("Magecraft", "0"));
    pcList.add(new TokenProperty("Religion", "0"));
    pcList.add(new TokenProperty("Theurgy", "0"));
    pcList.add(new TokenProperty("Obscura", "0"));
    pcList.add(new TokenProperty("Malis", "0"));
    pcList.add(new TokenProperty("History", "0"));
    pcList.add(new TokenProperty("Memory", "0"));
    pcList.add(new TokenProperty("Medicine", "0"));
    pcList.add(new TokenProperty("Investigation", "0"));
    pcList.add(new TokenProperty("Perception", "0"));
    pcList.add(new TokenProperty("Insight", "0"));
    pcList.add(new TokenProperty("Survival", "0"));
    pcList.add(new TokenProperty("Nature", "0"));
    pcList.add(new TokenProperty("Persuasion", "0"));
    pcList.add(new TokenProperty("Deception", "0"));
    pcList.add(new TokenProperty("Coercion", "0"));
    pcList.add(new TokenProperty("Performance", "0"));
    pcList.add(new TokenProperty("Artistry", "0"));
    //
    pcList.add(new TokenProperty("--------------------Skill Check Bonuses----"));
    pcList.add(new TokenProperty("AthleticsBonus", "0"));
    pcList.add(new TokenProperty("IntimidationBonus", "0"));
    pcList.add(new TokenProperty("AcrobaticsBonus", "0"));
    pcList.add(new TokenProperty("StealthBonus", "0"));
    pcList.add(new TokenProperty("SleightOfHandBonus", "0"));
    pcList.add(new TokenProperty("EnduranceBonus", "0"));
    pcList.add(new TokenProperty("ArcanaBonus", "0"));
    pcList.add(new TokenProperty("MagecraftBonus", "0"));
    pcList.add(new TokenProperty("ReligionBonus", "0"));
    pcList.add(new TokenProperty("TheurgyBonus", "0"));
    pcList.add(new TokenProperty("ObscuraBonus", "0"));
    pcList.add(new TokenProperty("MalisBonus", "0"));
    pcList.add(new TokenProperty("HistoryBonus", "0"));
    pcList.add(new TokenProperty("MemoryBonus", "0"));
    pcList.add(new TokenProperty("MedicineBonus", "0"));
    pcList.add(new TokenProperty("InvestigationBonus", "0"));
    pcList.add(new TokenProperty("PerceptionBonus", "0"));
    pcList.add(new TokenProperty("InsightBonus", "0"));
    pcList.add(new TokenProperty("SurvivalBonus", "0"));
    pcList.add(new TokenProperty("NatureBonus", "0"));
    pcList.add(new TokenProperty("PersuasionBonus", "0"));
    pcList.add(new TokenProperty("DeceptionBonus", "0"));
    pcList.add(new TokenProperty("CoercionBonus", "0"));
    pcList.add(new TokenProperty("PerformanceBonus", "0"));
    pcList.add(new TokenProperty("ArtistryBonus", "0"));
    //
    pcList.add(new TokenProperty("--------SAVING THROW BONUSES---", "----------"));
    pcList.add(new TokenProperty("STRbonus", "0"));
    pcList.add(new TokenProperty("DEXbonus", "0"));
    pcList.add(new TokenProperty("CONbonus", "0"));
    pcList.add(new TokenProperty("ARCbonus", "0"));
    pcList.add(new TokenProperty("DIVbonus", "0"));
    pcList.add(new TokenProperty("OCUbonus", "0"));
    pcList.add(new TokenProperty("INTbonus", "0"));
    pcList.add(new TokenProperty("WISbonus", "0"));
    pcList.add(new TokenProperty("CHAbonus", "0"));
    //
    pcList.add(new TokenProperty("--------------------DONT-TOUCH---:----------", ""));
    pcList.add(new TokenProperty("STRmod", "[r:floor((STR-10)/2)]"));
    pcList.add(new TokenProperty("DEXmod", "[r:floor((DEX-10)/2)]"));
    pcList.add(new TokenProperty("CONmod", "[r:floor((CON-10)/2)]"));
    pcList.add(new TokenProperty("ARCmod", "[r:floor((ARC-10)/2)]"));
    pcList.add(new TokenProperty("DIVmod", "[r:floor((DIV-10)/2)]"));
    pcList.add(new TokenProperty("OCUmod", "[r:floor((OCU-10)/2)]"));
    pcList.add(new TokenProperty("INTmod", "[r:floor((INT-10)/2)]"));
    pcList.add(new TokenProperty("WISmod", "[r:floor((WIS-10)/2)]"));
    pcList.add(new TokenProperty("CHAmod", "[r:floor((CHA-10)/2)]"));
    pcList.add(new TokenProperty("curMaxHP", "[r:maxHP-woundedHP]"));
    pcList.add(new TokenProperty("woundedHP", "0"));
    pcList.add(new TokenProperty("curHP", "17"));
    pcList.add(new TokenProperty("VITdice", "1"));
    pcList.add(new TokenProperty("maxVITdice", "[r:bonusVITdice+TotalLevel]"));
    pcList.add(
        new TokenProperty(
            "Vitality",
            null,
            true,
            true,
            true,
            "[r:CurHP] / [r:curMaxHP] [r:if(TempHP>0,\"| Ward: \"+TempHP,\"\")]"));
    pcList.add(
        new TokenProperty(
            "Attunement SLots", null, true, true, true, "[r:TotSlots-UsedSlots] / [r:TotSlots]"));
    pcList.add(
        new TokenProperty(
            "Proficiency", null, true, true, true, "[r:if(TotalLevel<3,1,floor(TotalLevel/3))]"));
    pcList.add(new TokenProperty("deathDC", "6"));
    pcList.add(
        new TokenProperty("FullCastLvl", "[r:Wizard + Sorcerer + Warlock + Druid + Cleric]"));
    pcList.add(
        new TokenProperty("HalfCastLvl", "[r:Ranger + Bard + Paladin + Shadow + Inquisitor]"));
    pcList.add(
        new TokenProperty(
            "TotalLevel",
            "[r:Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard+Wizard+Sorcerer+Warlock+Druid+Inquisitor+Cleric+Paladin]"));
    pcList.add(
        new TokenProperty(
            "TotSlots",
            "[r:BonusSlots+if((FullCastLvl+HalfCastLvl)>0,1,0)+floor(FullCastLvl/2)+floor(HalfCastLvl/4)+floor((FullCastLvl+HalfCastLvl)/21)]"));
    pcList.add(new TokenProperty("Passive Perception", null, true, true, true, "[r:10+WISmod]"));
    pcList.add(
        new TokenProperty(
            "ClassLevel",
            "CL[r:TotalLevel][r:if(TotalLevel>0,\"-\",\"\")][r:if(Warrior>0,\"Warrior:\"+Warrior,\"\")][r:if(Warrior<TotalLevel&&Warrior>0,\"-\",\"\")][r:if(Barbarian>0,\"Barbarian:\"+Barbarian,\"\")][r:if((Warrior+Barbarian)<TotalLevel&&Barbarian>0,\"-\",\"\")][r:if(Knight>0,\"Knight:\"+Knight,\"\")][r:if((Warrior+Barbarian+Knight)<TotalLevel&&Knight>0,\"-\",\"\")][r:if(Thief>0,\"Thief:\"+Thief,\"\")][r:if((Warrior+Barbarian+Knight+Thief)<TotalLevel&&Thief>0,\"-\",\"\")][r:if(Shadow>0,\"Shadow:\"+Shadow,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow)<TotalLevel&&Shadow>0,\"-\",\"\")][r:if(Ranger>0,\"Ranger:\"+Ranger,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger)<TotalLevel&&Ranger>0,\"-\",\"\")][r:if(Bard>0,\"Bard:\"+Bard,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard)<TotalLevel&&Bard>0,\"-\",\"\")][r:if(Wizard>0,\"Wizard:\"+Wizard,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard+Wizard)<TotalLevel&&Wizard>0,\"-\",\"\")][r:if(Sorcerer>0,\"Sorcerer:\"+Sorcerer,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard+Wizard+Sorcerer)<TotalLevel&&Sorcerer>0,\"-\",\"\")][r:if(Warlock>0,\"Warlock:\"+Warlock,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard+Wizard+Sorcerer+Warlock)<TotalLevel&&Warlock>0,\"-\",\"\")][r:if(Druid>0,\"Druid:\"+Druid,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard+Wizard+Sorcerer+Warlock+Druid)<TotalLevel&&Druid>0,\"-\",\"\")][r:if(Inquisitor>0,\"Inquisitor:\"+Inquisitor,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard+Wizard+Sorcerer+Warlock+Druid+Inquisitor)<TotalLevel&&Inquisitor>0,\"-\",\"\")][r:if(Cleric>0,\"Cleric:\"+Cleric,\"\")][r:if((Warrior+Barbarian+Knight+Thief+Shadow+Ranger+Bard+Wizard+Sorcerer+Warlock+Druid+Inquisitor+Cleric)<TotalLevel&&Cleric>0,\"-\",\"\")][r:if(Paladin>0,\"Paladin:\"+Paladin,\"\")]"));
    pcList.add(new TokenProperty("WardMax", "0"));
    pcList.add(new TokenProperty("Torpor", "0"));
    tokenTypeMap.put(DEFAULT_PC_TOKEN_PROPERTY_TYPE, pcList);
  }

  // pain.
  private void initTokenStatesMap() {
    tokenStates.clear();
    MD5Key blindKey = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2");
    FlowImageTokenOverlay blind = new FlowImageTokenOverlay("Blind", blindKey, 5);
    blind.setGroup("Debuffs");
    tokenStates.put("Blind", blind);

    MD5Key burnKey = new MD5Key("e1570b574fd40ecc5acdd53fde0cce90");
    FlowImageTokenOverlay burning = new FlowImageTokenOverlay("Burning", burnKey, 5);
    burning.setGroup("Debuffs");
    tokenStates.put("Burning", burning);

    MD5Key charmKey = new MD5Key("34156dee11a14b533f52162856e3a402");
    FlowImageTokenOverlay charmed = new FlowImageTokenOverlay("Charmed", charmKey, 5);
    charmed.setGroup("Debuffs");
    tokenStates.put("Charmed", charmed);

    MD5Key deafKey = new MD5Key("ffa3a03772423b9d55f1494b9a0d9619");
    FlowImageTokenOverlay deafened = new FlowImageTokenOverlay("Deafened", deafKey, 5);
    deafened.setGroup("Debuffs");
    tokenStates.put("Deafened", deafened);

    MD5Key frightKey = new MD5Key("15fbfb8fac353f2efca0cd611c516ffb");
    FlowImageTokenOverlay frightened = new FlowImageTokenOverlay("Frightened", frightKey, 5);
    frightened.setGroup("Debuffs");
    tokenStates.put("Frightened", frightened);

    MD5Key grappleKey = new MD5Key("fb66d33445d755b85938c0d4f8c40d38");
    FlowImageTokenOverlay grappled = new FlowImageTokenOverlay("Grappled", grappleKey, 5);
    grappled.setGroup("Debuffs");
    tokenStates.put("Grappled", grappled);

    MD5Key hiddenKey = new MD5Key("ea60af6985dba9ef8c8dfaad3d2c848f");
    FlowImageTokenOverlay hidden = new FlowImageTokenOverlay("Hidden", hiddenKey, 5);
    hidden.setGroup("General");
    tokenStates.put("Hidden", hidden);

    MD5Key incapKey = new MD5Key("94b1cbc2e0cd7f883cca0b060df3d198");
    FlowImageTokenOverlay incapacitated = new FlowImageTokenOverlay("Incapacitated", incapKey, 5);
    incapacitated.setGroup("General");
    tokenStates.put("Incapacitated", incapacitated);

    MD5Key invisKey = new MD5Key("f84c7457c492ed7444ff19da60d88c9b");
    FlowImageTokenOverlay invisible = new FlowImageTokenOverlay("Invisible", invisKey, 5);
    invisible.setGroup("General");
    tokenStates.put("Invisible", invisible);

    MD5Key langKey = new MD5Key("b2b4ce2cd1f3b9d2f8fbf4cd947a7681");
    FlowImageTokenOverlay languished = new FlowImageTokenOverlay("Languished", langKey, 5);
    languished.setGroup("Debuffs");
    tokenStates.put("Languished", languished);

    MD5Key paraKey = new MD5Key("658580370fc83d29e6332a4bf61b5daf");
    FlowImageTokenOverlay paralyzed = new FlowImageTokenOverlay("Paralyzed", paraKey, 5);
    paralyzed.setGroup("Debuffs");
    tokenStates.put("Paralyzed", paralyzed);

    MD5Key petriKey = new MD5Key("044b844ac3c12abb61d73647f58e1209");
    FlowImageTokenOverlay petrified = new FlowImageTokenOverlay("Petrified", petriKey, 5);
    petrified.setGroup("Debuffs");
    tokenStates.put("Petrified", petrified);

    MD5Key poisKey = new MD5Key("485d2924c6bfbd7cd76be33a1f8d36dd");
    FlowImageTokenOverlay poisoned = new FlowImageTokenOverlay("Poisoned", poisKey, 5);
    poisoned.setGroup("Debuffs");
    tokenStates.put("Poisoned", poisoned);

    MD5Key proneKey = new MD5Key("ab94b806d7383139a360c76258bdb9eb");
    FlowImageTokenOverlay prone = new FlowImageTokenOverlay("Prone", proneKey, 5);
    prone.setGroup("General");
    tokenStates.put("Prone", prone);

    MD5Key restrKey = new MD5Key("68a4f455d60af29b082f071165ae5ed8");
    FlowImageTokenOverlay restrained = new FlowImageTokenOverlay("Restrained", restrKey, 5);
    restrained.setGroup("Debuffs");
    tokenStates.put("Restrained", restrained);

    MD5Key ruinKey = new MD5Key("9b7663d403a82c12fa394fe849f2bd14");
    FlowImageTokenOverlay ruined = new FlowImageTokenOverlay("Ruined", ruinKey, 5);
    ruined.setGroup("Debuffs");
    tokenStates.put("Ruined", ruined);

    MD5Key shockKey = new MD5Key("493007017372a9e0caaf68601fc2a73d");
    FlowImageTokenOverlay shocked = new FlowImageTokenOverlay("Shocked", shockKey, 5);
    shocked.setGroup("Debuffs");
    tokenStates.put("Shocked", shocked);

    MD5Key staggKey = new MD5Key("c738228879ce465345c79b0a4009bf9f");
    FlowImageTokenOverlay staggered = new FlowImageTokenOverlay("Staggered", staggKey, 5);
    staggered.setGroup("Debuffs");
    tokenStates.put("Staggered", staggered);

    MD5Key stunKey = new MD5Key("383d66bd6681fc3b04bc16b0f24a8f13");
    FlowImageTokenOverlay stunned = new FlowImageTokenOverlay("Stunned", stunKey, 5);
    stunned.setGroup("Debuffs");
    tokenStates.put("Stunned", stunned);

    MD5Key tauntKey = new MD5Key("bab870e7803d7d8da0df29714ead03aa");
    FlowImageTokenOverlay taunted = new FlowImageTokenOverlay("Taunted", tauntKey, 5);
    taunted.setGroup("Debuffs");
    tokenStates.put("Taunted", taunted);

    MD5Key withKey = new MD5Key("03d6dea1b971e511ffd3d3b3c1341fc3");
    FlowImageTokenOverlay withered = new FlowImageTokenOverlay("Withered", withKey, 5);
    withered.setGroup("Debuffs");
    tokenStates.put("Withered", withered);

    MD5Key ex1Key = new MD5Key("6fd8d1b5b95ac97b99a1bf5e534d52dd");
    FlowImageTokenOverlay ex1 = new FlowImageTokenOverlay("I", ex1Key, 5);
    ex1.setGroup("Exhaustion");
    tokenStates.put("I", ex1);

    MD5Key ex2Key = new MD5Key("3a06a0282b5a0102c8dd558dc7b03252");
    FlowImageTokenOverlay ex2 = new FlowImageTokenOverlay("II", ex2Key, 5);
    ex2.setGroup("Exhaustion");
    tokenStates.put("II", ex2);

    MD5Key ex3Key = new MD5Key("c0cbead07a2cee2ac526654e2c368f53");
    FlowImageTokenOverlay ex3 = new FlowImageTokenOverlay("III", ex3Key, 5);
    ex3.setGroup("Exhaustion");
    tokenStates.put("III", ex3);

    MD5Key ex4Key = new MD5Key("671ea3b46f87a2f005082ca5bfd2b5cf");
    FlowImageTokenOverlay ex4 = new FlowImageTokenOverlay("IV", ex4Key, 5);
    ex4.setGroup("Exhaustion");
    tokenStates.put("IV", ex4);

    MD5Key ex5Key = new MD5Key("c0104b92e524c5eeb093ef3188ebf668");
    FlowImageTokenOverlay ex5 = new FlowImageTokenOverlay("V", ex5Key, 5);
    ex5.setGroup("Exhaustion");
    tokenStates.put("V", ex5);

    MD5Key ex6Key = new MD5Key("19864fe82a6cf9bdf08d345159f077fa");
    ImageTokenOverlay ex6 = new ImageTokenOverlay("VI", ex6Key);
    ex6.setGroup("Exhaustion");
    tokenStates.put("VI", ex6);

    MD5Key concKey = new MD5Key("88fb57b171df0489372b83051d8d84c8");
    FlowImageTokenOverlay concentrating = new FlowImageTokenOverlay("Concentrating", concKey, 5);
    concentrating.setGroup("General");
    tokenStates.put("Concentrating", concentrating);

    MD5Key afkKey = new MD5Key("b8a18ee0963c8c25b4fb512557742a71");
    ImageTokenOverlay afk = new ImageTokenOverlay("AFK", afkKey);
    tokenStates.put("AFK", afk);

    MD5Key deadKey = new MD5Key("724139a5ae1c1a9b12512d4d20499d3a");
    ImageTokenOverlay dead = new ImageTokenOverlay("Dead", deadKey);
    tokenStates.put("Dead", dead);

    MD5Key zerooneKey = new MD5Key("00a9806b816bd2a68f4b018efeb4eab2");
    CornerImageTokenOverlay zero =
        new CornerImageTokenOverlay("_0", zerooneKey, AbstractTemplate.Quadrant.SOUTH_EAST);
    zero.setGroup("Elevation");
    tokenStates.put("_0", zero);

    MD5Key unconKey = new MD5Key("597ecd26c168c5b5205857031c89b50b");
    ImageTokenOverlay uncon = new ImageTokenOverlay("Unconscious", unconKey);
    uncon.setGroup("General");
    tokenStates.put("Unconscious", uncon);
    /**
     * MD5Key zerotKey = new MD5Key("a0d83ff5ab1fa4230e1652614a8a1832"); CornerImageTokenOverlay
     * tzero = new CornerImageTokenOverlay("_0f", zerotKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * tzero.setGroup("Elevation"); tokenStates.put("_0f",tzero);
     *
     * <p>MD5Key onehKey = new MD5Key("df61aa46767f3336870c4cbcc768ba89"); CornerImageTokenOverlay
     * oneh = new CornerImageTokenOverlay("_1h", onehKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * oneh.setGroup("Elevation"); tokenStates.put("_1h",oneh);
     *
     * <p>MD5Key twohKey = new MD5Key("1c6324a0e04531510d216cd3854df48b"); CornerImageTokenOverlay
     * twoh = new CornerImageTokenOverlay("_2h", twohKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * twoh.setGroup("Elevation"); tokenStates.put("_2h",twoh);
     *
     * <p>MD5Key threehKey = new MD5Key("46e7bc999603f9db355eba3178ab5554"); CornerImageTokenOverlay
     * threeh = new CornerImageTokenOverlay("_3h", threehKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * threeh.setGroup("Elevation"); tokenStates.put("_3h",threeh);
     *
     * <p>MD5Key fourhKey = new MD5Key("dbee82d1185de697d27e41eba0270ce8"); CornerImageTokenOverlay
     * fourh = new CornerImageTokenOverlay("_4h", fourhKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * fourh.setGroup("Elevation"); tokenStates.put("_4h",fourh);
     *
     * <p>MD5Key fhKey = new MD5Key("63cee97304bed525f5255fd66c037c81"); CornerImageTokenOverlay fh
     * = new CornerImageTokenOverlay("_5h", fhKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * fh.setGroup("Elevation"); tokenStates.put("_05h",fh);
     *
     * <p>MD5Key shKey = new MD5Key("aebc7df5725e43e4cd59f9c42e0b2d95"); CornerImageTokenOverlay sh
     * = new CornerImageTokenOverlay("_6h", shKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * sh.setGroup("Elevation"); tokenStates.put("_6h",sh);
     *
     * <p>MD5Key sehKey = new MD5Key("f96662f16a4df317e0c96da3d5369265"); CornerImageTokenOverlay
     * seh = new CornerImageTokenOverlay("_7h", sehKey, AbstractTemplate.Quadrant.SOUTH_EAST);
     * seh.setGroup("Elevation"); tokenStates.put("_7h",seh);
     *
     * <p>MD5Key bKey = new MD5Key("e75c16c39978487a20f8776724635505"); CornerImageTokenOverlay b =
     * new CornerImageTokenOverlay("b", bKey); b.setGroup("Elevation"); tokenStates.put("b",b);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     *
     * <p>MD5Key Key = new MD5Key("0e935282a9c4d2d8674b90dcb1c6a3e2"); CornerImageTokenOverlay h =
     * new CornerImageTokenOverlay("", Key); .setGroup("Elevation"); tokenStates.put("",);
     */
  }

  private void initTokenBarsMap() {
    tokenBars.clear();
    MD5Key healthBase = new MD5Key("91ba043e19271c0e41a99fa416578bb8");
    MD5Key healthTop = new MD5Key("548cc5358c3cd16842bb8b967e4a5f40");
    MD5Key wardBase = new MD5Key("8277778d42b56bf5611013806ca0a12a");
    MD5Key wardTop = new MD5Key("fae6cd4a0f84cfcb2dd0d8cd10490003");
    MD5Key torpBase = new MD5Key("7f1355036afa9bbe5c9abd4480d5804d");
    MD5Key torpTop = new MD5Key("8ca11abc21ffefba60233b2efa1db917");

    TwoImageBarTokenOverlay health = new TwoImageBarTokenOverlay("Health", healthTop, healthBase);
    TwoImageBarTokenOverlay npcHealth =
        new TwoImageBarTokenOverlay("NPCHealth", healthTop, healthBase);
    TwoImageBarTokenOverlay torp = new TwoImageBarTokenOverlay("Torpor", torpTop, torpBase);
    TwoImageBarTokenOverlay npcTorp = new TwoImageBarTokenOverlay("NPCTorpor", torpTop, torpBase);
    TwoImageBarTokenOverlay ward = new TwoImageBarTokenOverlay("Ward", wardTop, wardBase);
    TwoImageBarTokenOverlay npcWard = new TwoImageBarTokenOverlay("NPCWard", wardTop, wardBase);

    health.setIncrements(100);
    npcHealth.setIncrements(100);
    torp.setIncrements(100);
    npcTorp.setIncrements(100);
    ward.setIncrements(100);
    npcWard.setIncrements(100);

    health.setShowOthers(false);
    npcHealth.setShowOthers(false);
    torp.setShowOthers(false);
    npcTorp.setShowOthers(false);
    ward.setShowOthers(false);
    npcWard.setShowOthers(false);
    health.setShowGM(true);
    npcHealth.setShowGM(true);
    torp.setShowGM(true);
    npcTorp.setShowGM(true);
    ward.setShowGM(true);
    npcWard.setShowGM(true);
    health.setShowOwner(true);
    npcHealth.setShowOwner(true);
    torp.setShowOwner(true);
    npcTorp.setShowOwner(true);
    ward.setShowOwner(true);
    npcWard.setShowOwner(true);

    tokenBars.put("Health", health);
    tokenBars.put("NPCHealth", npcHealth);
    tokenBars.put("Torpor", torp);
    tokenBars.put("NPCTorpor", npcTorp);
    tokenBars.put("Ward", ward);
    tokenBars.put("NPCWard", npcWard);
  }

  private void initCharacterSheetsMap() {
    characterSheets.clear();
    characterSheets.put("Basic", "net/rptools/maptool/client/ui/forms/basicCharacterSheet.xml");
  }

  public Set<MD5Key> getAllImageAssets() {
    Set<MD5Key> set = new HashSet<>();

    // Start with the table images
    for (LookupTable table : getLookupTableMap().values()) {
      set.addAll(table.getAllAssetIds());
    }

    // States have images as well
    for (AbstractTokenOverlay overlay : getTokenStatesMap().values()) {
      if (overlay instanceof ImageTokenOverlay) set.add(((ImageTokenOverlay) overlay).getAssetId());
    }

    // Bars
    for (BarTokenOverlay overlay : getTokenBarsMap().values()) {
      if (overlay instanceof SingleImageBarTokenOverlay) {
        set.add(((SingleImageBarTokenOverlay) overlay).getAssetId());
      } else if (overlay instanceof TwoImageBarTokenOverlay) {
        set.add(((TwoImageBarTokenOverlay) overlay).getTopAssetId());
        set.add(((TwoImageBarTokenOverlay) overlay).getBottomAssetId());
      } else if (overlay instanceof MultipleImageBarTokenOverlay) {
        set.addAll(Arrays.asList(((MultipleImageBarTokenOverlay) overlay).getAssetIds()));
      }
    }
    return set;
  }

  /** @return Getter for initiativeOwnerPermissions */
  public boolean isInitiativeOwnerPermissions() {
    return initiativeOwnerPermissions;
  }

  /** @param initiativeOwnerPermissions Setter for initiativeOwnerPermissions */
  public void setInitiativeOwnerPermissions(boolean initiativeOwnerPermissions) {
    this.initiativeOwnerPermissions = initiativeOwnerPermissions;
  }

  /** @return Getter for initiativeMovementLock */
  public boolean isInitiativeMovementLock() {
    return initiativeMovementLock;
  }

  /** @param initiativeMovementLock Setter for initiativeMovementLock */
  public void setInitiativeMovementLock(boolean initiativeMovementLock) {
    this.initiativeMovementLock = initiativeMovementLock;
  }

  public boolean isInitiativeUseReverseSort() {
    return initiativeUseReverseSort;
  }

  public void setInitiativeUseReverseSort(boolean initiativeUseReverseSort) {
    this.initiativeUseReverseSort = initiativeUseReverseSort;
  }

  public boolean isInitiativePanelButtonsDisabled() {
    return initiativePanelButtonsDisabled;
  }

  public void setInitiativePanelButtonsDisabled(boolean initiativePanelButtonsDisabled) {
    this.initiativePanelButtonsDisabled = initiativePanelButtonsDisabled;
  }

  /**
   * Getter for characterSheets. Only called by {@link Campaign#getCharacterSheets()} and that
   * function is never used elsewhere within MapTool. Yet. ;-)
   *
   * @return a Map of the characterSheets
   */
  public Map<String, String> getCharacterSheets() {
    return characterSheets;
  }

  /** @param characterSheets Setter for characterSheets */
  public void setCharacterSheets(Map<String, String> characterSheets) {
    this.characterSheets.clear();
    this.characterSheets.putAll(characterSheets);
  }

  protected Object readResolve() {
    if (tokenTypeMap == null) {
      tokenTypeMap = new HashMap<>();
    }
    if (remoteRepositoryList == null) {
      remoteRepositoryList = new ArrayList<>();
    }
    if (lightSourcesMap == null) {
      lightSourcesMap = new TreeMap<>();
    }
    if (lookupTableMap == null) {
      lookupTableMap = new HashMap<>();
    }
    if (sightTypeMap == null) {
      sightTypeMap = new HashMap<>();
    }
    if (tokenStates == null) {
      tokenStates = new LinkedHashMap<>();
    }
    if (tokenBars == null) {
      tokenBars = new LinkedHashMap<>();
    }
    if (characterSheets == null) {
      characterSheets = new HashMap<>();
    }
    return this;
  }

  public static CampaignProperties fromDto(CampaignPropertiesDto dto) {
    var props = new CampaignProperties();
    var tokenTypes = dto.getTokenTypesMap();
    tokenTypes.forEach(
        (k, v) ->
            props.tokenTypeMap.put(
                k,
                v.getPropertiesList().stream()
                    .map(TokenProperty::fromDto)
                    .collect(Collectors.toList())));
    if (dto.hasDefaultSightType()) {
      props.defaultSightType = dto.getDefaultSightType().getValue();
    }
    dto.getTokenStatesList()
        .forEach(
            s -> {
              var overlay = BooleanTokenOverlay.fromDto(s);
              props.tokenStates.put(overlay.getName(), overlay);
            });
    dto.getTokenBarsList()
        .forEach(
            b -> {
              var overlay = BarTokenOverlay.fromDto(b);
              props.tokenBars.put(overlay.getName(), overlay);
            });
    props.characterSheets.putAll(dto.getCharacterSheetsMap());
    props.initiativeOwnerPermissions = dto.getInitiativeOwnerPermissions();
    props.initiativeMovementLock = dto.getInitiativeMovementLock();
    props.initiativeUseReverseSort = dto.getInitiativeUseReverseSort();
    props.initiativePanelButtonsDisabled = dto.getInitiativePanelButtonsDisabled();
    dto.getLightSourcesMap()
        .forEach(
            (k, v) -> {
              var map = new HashMap<GUID, LightSource>();
              v.getLightSourcesList()
                  .forEach(
                      l -> {
                        var lightSource = LightSource.fromDto(l);
                        map.put(lightSource.getId(), lightSource);
                      });
              props.lightSourcesMap.put(k, map);
            });
    props.remoteRepositoryList.addAll(dto.getRemoteRepositoriesList());
    dto.getLookupTablesList()
        .forEach(
            lt -> {
              var table = LookupTable.fromDto(lt);
              props.lookupTableMap.put(table.getName(), table);
            });
    dto.getSightTypesList()
        .forEach(
            st -> {
              var sightType = SightType.fromDto(st);
              props.sightTypeMap.put(sightType.getName(), sightType);
            });
    return props;
  }

  public CampaignPropertiesDto toDto() {
    var dto = CampaignPropertiesDto.newBuilder();
    tokenTypeMap.forEach(
        (k, v) ->
            dto.putTokenTypes(
                k,
                TokenPropertyListDto.newBuilder()
                    .addAllProperties(
                        v.stream().map(TokenProperty::toDto).collect(Collectors.toList()))
                    .build()));
    if (defaultSightType != null) {
      dto.setDefaultSightType(StringValue.of(defaultSightType));
    }
    dto.addAllTokenStates(
        tokenStates.values().stream().map(BooleanTokenOverlay::toDto).collect(Collectors.toList()));
    dto.addAllTokenBars(
        tokenBars.values().stream().map(BarTokenOverlay::toDto).collect(Collectors.toList()));
    dto.putAllCharacterSheets(characterSheets);
    dto.setInitiativeOwnerPermissions(initiativeOwnerPermissions);
    dto.setInitiativeMovementLock(initiativeMovementLock);
    dto.setInitiativeUseReverseSort(initiativeUseReverseSort);
    dto.setInitiativePanelButtonsDisabled(initiativePanelButtonsDisabled);
    lightSourcesMap.forEach(
        (k, v) ->
            dto.putLightSources(
                k,
                LightSourceListDto.newBuilder()
                    .addAllLightSources(
                        v.values().stream().map(LightSource::toDto).collect(Collectors.toList()))
                    .build()));
    dto.addAllRemoteRepositories(remoteRepositoryList);
    dto.addAllLookupTables(
        lookupTableMap.values().stream().map(LookupTable::toDto).collect(Collectors.toList()));
    dto.addAllSightTypes(
        sightTypeMap.values().stream().map(SightType::toDto).collect(Collectors.toList()));
    return dto.build();
  }
}
