# compile
# $** All dependents of the current target.
# $*  Current target's path and base name without a file name extension.
# $?  All dependents with a later time stamp than the current target.
# $@  Current target's path and base name with a file name extension.
# $(*F) represents the targets base name.
# $(*R) represents the targets base name and directory.
# $(**R) represents all the dependents including directories.
#index.exe : index.cc lex.yy.c tokens.lex
#	$(CC) $< -o $@ $(CPPFLAGS)
#$(OB)%.o : %.cc ; $(CC) -c $< -o $@ $(CPPFLAGS)
#"C:\Archivos de programa\WinZip\WinZip Self-Extractor\wzipse32.exe" so_vXXX.zip @DEBUG\wzse_options.txt


# Macros
JC = javac

CD  = so/config/
DD  = so/data/
GD  = so/gui/
GRD = so/gui/render/
TD  = so/text/
UD  = so/util/
FPD = so/gui/flagsplugin/
UPD = so/updater/
LD  = so/launcher/

KEYSTORE = -keystore DEBUG\so.keystore
STOREPASS = -storepass l1ndh0lm

# inference rules
.SUFFIXES: .class .java

.java.class:
	$(JC) $*.java

# Compile
classes: so/So.class $(GD)MainFrame.class resources updater launcher

#other
resources: resources/default.res resources/ntdb_urls.res resources/countrydata.res

resources/default.res : languages/^#default^#.txt
	copy languages\#default#.txt resources\default.res /Y

resources/ntdb_urls.res: resources/ntdb_urls.txt
	copy resources\ntdb_urls.txt resources\ntdb_urls.res /Y

resources/countrydata.res: resources/countrydata.txt
	copy resources\countrydata.txt resources\countrydata.res /Y

# *********************************************************************
# package so
# constants
constants : so/Constants.class

#main
so/So.class : constants $(GD)LoadingFrame.class so/So.java

$(GD)MainFrame.class : constants utils text config data gui flagsplugin $(GD)MainFrame.java

# *********************************************************************
# package so/updater
updater : $(UPD)Updater.class

launcher : $(LD)Launcher.class

# *********************************************************************
# package so/config
config : $(CD)CountryUtils.class $(CD)Options.class $(CD)PositionManager.class

$(CD)CountryUtils.class : constants $(UD)DebugFrame.class $(CD)CountryUtils.java

$(CD)Options.class : constants utils $(DD)AbstractData.class $(DD)TableColumnData.class $(DD)CountryDataUnit.class $(TD)LabelManager.class $(GRD)CountryListCellRenderer.class $(CD)Options.java

$(CD)PositionManager.class : constants $(DD)AbstractData.class $(TD)LabelManager.class $(DD)PlayerProfile.class $(DD)PlayerRoster.class $(DD)DataPair.class $(CD)PositionManager.java


# *********************************************************************
# package so/data
data : $(DD)Noteable.class $(DD)CountryDataUnit.class $(DD)Score.class $(DD)TableColumnData.class $(DD)Stadium.class $(DD)MatchIdParser.class $(DD)AbstractData.class $(DD)DataPair.class $(DD)CoachOffice.class $(DD)MatchRepository.class $(DD)JuniorProfile.class $(DD)JuniorSchool.class $(DD)PlayerProfile.class $(DD)PlayerRoster.class $(DD)TeamDetails.class $(DD)LeagueDetails.class $(DD)LeagueEncapsulator.class $(DD)DataEncapsulator.class $(DD)HTMLHandler.class $(DD)XMLHandler.class $(DD)NTDBHandler.class $(DD)LineupManager.class

$(TD)MatchIdParser.class : constants $(UD)Utils.class $(TD)MatchIdParser.java

$(DD)AbstractData.class : constants $(DD)AbstractData.java

$(DD)DataPair.class : constants $(DD)DataPair.java

$(DD)CoachOffice.class : constants $(DD)CoachOffice.java

$(DD)MatchRepository.class : $(DD)AbstractData.class $(DD)Score.class $(UD)SokkerWeek.class $(DD)MatchRepository.java

$(DD)DataEncapsulator.class : $(DD)AbstractData.class $(DD)TeamDetails.class $(DD)PlayerRoster.class $(DD)JuniorSchool.class $(DD)Stadium.class $(DD)CoachOffice.class $(DD)LineupManager.class $(DD)DataEncapsulator.java

$(DD)LeagueEncapsulator.class : $(DD)AbstractData.class $(DD)LeagueDetails.class $(DD)LeagueEncapsulator.java

$(DD)LeagueDetails.class : constants $(DD)LeagueDetails.java

$(DD)HTMLHandler.class : constants utils $(TD)LabelManager.class $(CD)Options.class $(DD)HTMLHandler.java

$(DD)JuniorProfile.class : constants $(UD)SokkerCalendar.class $(DD)DataPair.class $(DD)JuniorProfile.java

$(DD)JuniorSchool.class : constants $(DD)JuniorProfile.class $(DD)JuniorSchool.java

$(DD)PlayerProfile.class : constants $(DD)Noteable.class $(UD)SokkerCalendar.class $(DD)DataPair.class $(DD)PlayerProfile.java

$(DD)PlayerRoster.class : constants $(DD)PlayerProfile.class $(DD)PlayerRoster.java

$(DD)TeamDetails.class : constants $(DD)Noteable.class $(DD)TeamDetails.java

$(DD)XMLHandler.class : constants $(UD)DebugFrame.class $(UD)Dialog.class $(UD)Utils.class $(GD)ProgressDialog.class $(TD)LabelManager.class $(CD)Options.class $(DD)TeamDetails.class $(DD)PlayerProfile.class $(DD)JuniorProfile.class $(DD)Stadium.class $(DD)CoachOffice.class $(DD)XMLHandler.java

$(DD)NTDBHandler.class : constants $(UD)DebugFrame.class $(GD)ProgressDialog.class $(TD)LabelManager.class $(CD)Options.class $(DD)PlayerProfile.class $(DD)NTDBHandler.java

# *********************************************************************
# package so/gui
gui : $(GD)ProgressDialog.class $(GD)LoadingFrame.class $(GD)FaceCanvas.class $(GD)LeagueGraphPanel.class $(GD)GraphPanel.class $(GD)JuniorSchoolPanel.class $(GD)SquadPanel.class $(GD)PlayerStatsPanel.class $(GD)LeagueTablePanel.class $(GD)TrainingPanel.class $(GD)CurrencyConversionFrame.class $(GD)NotesEditorDialog.class $(GD)MatchesPanel.class $(GD)LineupPanel.class

$(GD)FaceCanvas.class : constants $(GD)FaceCanvas.java

$(GD)JuniorSchoolPanel.class : constants $(DD)TableColumnData.class $(DD)DataPair.class $(DD)JuniorSchool.class $(GRD)DataPairCellRenderer.class $(TD)LabelManager.class $(CD)Options.class $(GRD)NumberCellRenderer.class $(GD)GraphPanel.class $(GD)JuniorSchoolPanel.java

$(GD)SquadPanel.class : constants $(UD)TableSorter.class $(UD)FormattedDateHolder.class $(DD)TableColumnData.class $(DD)DataPair.class $(DD)PlayerRoster.class $(GRD)CountryFlagCellRenderer.class $(GRD)DataPairCellRenderer.class $(GRD)PositionCellRenderer.class $(GRD)SquadIconCellRenderer.class $(TD)LabelManager.class $(CD)Options.class $(GD)SquadPanel.java

$(GD)PlayerStatsPanel.class : constants $(UD)FormattedDateHolder.class $(GD)FaceCanvas.class $(GD)GraphPanel.class $(DD)PlayerRoster.class $(TD)LabelManager.class $(CD)Options.class $(GD)PlayerStatsPanel.java

$(GD)GraphPanel.class : $(UD)SokkerWeek.class $(DD)PlayerProfile.class $(GD)GraphPanel.java

$(GD)LeagueGraphPanel.class : constants $(GD)LeagueGraphPanel.java

$(GD)LeagueTablePanel.class : constants $(TD)LabelManager.class $(CD)Options.class $(GD)LeagueGraphPanel.class $(DD)LeagueDetails.class $(GD)LeagueTablePanel.java

$(GD)TrainingPanel.class : constants $(DD)CoachOffice.class $(GRD)CountryFlagCellRenderer.class $(TD)LabelManager.class $(CD)Options.class $(GD)TrainingPanel.java

$(GD)MatchesPanel.class : constants $(TD)LabelManager.class $(CD)Options.class $(DD)MatchRepository.class $(GD)MatchesPanel.java

$(GD)CurrencyConversionFrame.class : $(TD)LabelManager.class $(CD)Options.class $(CD)CountryUtils.class $(DD)CountryDataUnit.class $(GRD)CountryListCellRenderer.class $(GD)CurrencyConversionFrame.java

$(GD)NotesEditorDialog.class : $(UD)Dialog.class $(DD)PlayerProfile.class $(GD)NotesEditorDialog.java

$(GD)LineupPanel.class : constants $(DD)LineupManager.class $(GRD)SquadIconCellRenderer.class $(GD)LineupPanel.java

# *********************************************************************
# package so/gui/render
render : $(GRD)CountryListCellRenderer.class $(GRD)DataPairCellRenderer.class $(GRD)NumberCellRenderer.class $(GRD)PositionCellRenderer.class $(GRD)CountryFlagCellRenderer.class $(GRD)SquadIconCellRenderer.class

$(GRD)CountryListCellRenderer.class : $(DD)CountryDataUnit.class $(GRD)CountryListCellRenderer.java

$(GRD)DataPairCellRenderer.class : constants $(DD)DataPair.class $(CD)Options.class $(GRD)DataPairCellRenderer.java

$(GRD)NumberCellRenderer.class : constants $(CD)Options.class $(GRD)NumberCellRenderer.java

$(GRD)PositionCellRenderer.class : constants $(DD)DataPair.class $(TD)LabelManager.class $(CD)Options.class $(GRD)PositionCellRenderer.java

# *********************************************************************
# package so/text
text : $(TD)LabelManager.class $(TD)ReportFrame.class

$(TD)LabelManager.class : constants utils $(DD)CountryDataUnit.class $(TD)LabelManager.java

$(TD)ReportFrame.class : constants $(TD)ReportFrame.java

# *********************************************************************
# package so/utils
utils : $(UD)Utils.class $(UD)SokkerCalendar.class $(UD)SokkerWeek.class $(UD)DebugFrame.class $(UD)CookieManager.class $(UD)ExampleFileFilter.class $(UD)TableSorter.class $(UD)SwingWorker.class $(UD)FormattedDateHolder.class $(UD)DebugOutputStream.class $(UD)Dialog.class $(UD)SokkerViewerFileFilter.class $(UD)ApolloFileFilter.class

$(UD)DebugOutputStream.class : $(UD)DebugFrame.class $(UD)DebugOutputStream.java

$(UD)SokkerWeek.class : $(UD)SokkerCalendar.class $(UD)SokkerWeek.java

# *********************************************************************
# package so/gui/flagsplugin
flagsplugin : $(FPD)FlagLabel.class $(FPD)FlagRenderer.class $(FPD)PlayersFlagPanel.class $(FPD)FlagGridPanel.class $(FPD)FlagsPlugin.class

$(FPD)FlagLabel.class : $(DD)CountryDataUnit.class $(FPD)FlagLabel.java

$(FPD)PlayersFlagPanel.class : $(UD)Dialog.class $(DD)CountryDataUnit.class $(TD)LabelManager.class $(DD)PlayerProfile.class $(DD)PlayerRoster.class  $(FPD)PlayersFlagPanel.java

$(FPD)FlagGridPanel.class : $(UD)Dialog.class $(DD)CountryDataUnit.class $(TD)LabelManager.class $(FPD)FlagLabel.class $(FPD)FlagGridPanel.java

$(FPD)FlagRenderer.class : $(DD)CountryDataUnit.class $(FPD)FlagGridPanel.class $(FPD)FlagRenderer.java

$(FPD)FlagsPlugin.class : $(DD)CountryDataUnit.class $(TD)LabelManager.class $(CD)Options.class $(DD)TeamDetails.class $(DD)PlayerRoster.class $(DD)MatchRepository.class $(FPD)PlayersFlagPanel.class $(FPD)FlagGridPanel.class $(FPD)FlagRenderer.class $(FPD)FlagsPlugin.java



# *********************************************************************
# *********************************************************************
# *********************************************************************

all : jar zip

langs : MORE\language_files.zip MORE\langs.jar

zip : source.zip MORE\language_files.zip so_vXXX.zip so_vXXX.exe

jar : MORE\langs.jar MORE\settings.jar resources\flags.jar resources\faces.jar so.jar resources\upd.jar resources\bin.jar

source.zip : *.txt *.bat so/*.java so/data/*.java so/gui/*.java so/config/*.java so/text/*.java Makefile
	WzZIP -a -P -r -xDEBUG\*.* -xMORE\*.* source.zip *.java *.txt *.bat Makefile

MORE\language_files.zip : languages\*.txt
	WzZIP -a MORE\language_files.zip languages\*.txt

MORE\langs.jar : languages\*.txt
	jar cf MORE\langs.jar languages\*.txt
	jarsigner $(KEYSTORE) $(STOREPASS) MORE\langs.jar danny

resources\langlist.txt : MORE\langs.jar
	jar tf MORE\langs.jar languages\*.txt > resources\langlist.txt

MORE\settings.jar : resources\countrydata.txt resources\langlist.txt resources\ntdb_urls.txt
	jar cvf MORE\settings.jar $**
	jarsigner $(KEYSTORE) $(STOREPASS) MORE\settings.jar danny

sodebug.jar : DEBUG\manifest_bin.txt so\*.class so\config\*.class so\data\*.class so\gui\*.class so\gui\render\*.class so\text\*.class so\util\*.class resources\default.res resources\countrydata.res resources\ntdb_urls.res
	jar cvf0m sodebug.jar DEBUG\manifest_bin.txt so\*.class so\config\*.class so\data\*.class so\gui\*.class so\gui\render\*.class so\text\*.class so\util\*.class so\gui\flagsplugin\*.class com\toedter\calendar\*.class com\toedter\components\*.class images\*.png images\*.gif com\toedter\calendar\images\*.* resources\default.res resources\countrydata.res resources\ntdb_urls.res

so.jar : DEBUG\manifest_so.txt so\launcher\Launcher.class
	jar cvf0m $@ DEBUG\manifest_so.txt so\launcher\Launcher.class
	jarsigner $(KEYSTORE) $(STOREPASS) $@ danny

resources\upd.jar : DEBUG\manifest_upd.txt so\updater\*.class
	jar cvf0 $@ so\updater\*.class
	jarsigner $(KEYSTORE) $(STOREPASS) $@ danny

resources\flags.jar : images\flags\*.*
	jar cvf0 $@ images\flags\*.*
	jarsigner $(KEYSTORE) $(STOREPASS) $@ danny

resources\faces.jar : images\faces\*.*
	jar cvf0 $@ images\faces\*.*
	jarsigner $(KEYSTORE) $(STOREPASS) $@ danny

resources\bin.jar : sodebug.jar
	java -jar DEBUG/bin/retroguard.jar sodebug.jar $@ DEBUG/script.rgs.txt DEBUG/so_ofu.log
	jarsigner $(KEYSTORE) $(STOREPASS) $@ danny

so_vXXX.zip : so.jar resources\bin.jar resources\upd.jar resources\faces.jar resources\flags.jar changes.txt overrides.txt
	@del $@ > nul
	WzZIP -a -P -r -xDEBUG\*.* -xMORE\*.* -xresources\default.txt -xresources\langlist.txt -xresources\plaf\*.jar -xdata\*.* $@ *.txt so.jar so.ico resources\*.jar Readme.html
	WzZIP -a -P $@ resources\plaf\nimrodlf.jar

so_vXXX.exe : so_vXXX.zip
	@del $@ > nul
	"C:\Archivos de Programa\WinZip Self-Extractor\wzipse32.exe" so_vXXX.zip @DEBUG\wzse_options.txt


