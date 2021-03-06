                                                                wwsafe  8  �9=�Cp@��bnر�5�׬ѧ*���%�J�l.J�;[$����*yc�@����kZ�w�"�g�<K8�����B��A���*c������<��X1��5��w0����\�g��!��S�	 ��N	�c� �$;�3@;<���x�|n~����7M����l�S(��I�;sߚu���h����֥:�,��y��aˎ�fd�ӣ6��!���K� 0D�x�5�]Ge�X�ت�F��8i=�2{]��#j���A[�:��b��K!��m=ZYFOh�@t,αS�r\��H��"H����[�LF`-----------------------
set appjar=findbugs.jar
set javahome=
set launcher=java.exe
set start=start "FindBugs"
set jvmargs=
set debugArg=
set conserveSpaceArg=
set workHardArg=
set args=
set javaProps=
set maxheap=768

REM default UI is gui2
set launchUI=2

:: Try finding the default FINDBUGS_HOME directory
:: from the directory path of this script
set default_findbugs_home=%~dp0..

:: Honor JAVA_HOME environment variable if it is set
if "%JAVA_HOME%"=="" goto nojavahome
if not exist "%JAVA_HOME%\bin\javaw.exe" goto nojavahome
set javahome=%JAVA_HOME%\bin\
:nojavahome

goto loop

:: ----------------------------------------------------------------------
:: Process command-line arguments
:: ----------------------------------------------------------------------

:shift2
shift
:shift1
shift

:loop

:: Remove surrounding quotes from %1 and %2
set firstArg=%~1
set secondArg=%~2

if "%firstArg%"=="" goto launch

:: AddMessages
if not "%firstArg%"=="-addMessages" goto notAddMessages
set fb_mainclass=edu.umd.cs.findbugs.AddMessages
goto shift1
:notAddMessages

:: computeBugHistory
if not "%firstArg%"=="-computeBugHistory" goto notUpdate
set fb_mainclass=edu.umd.cs.findbugs.workflow.Update
goto shift1
:notUpdate

:: convertXmlToText
if not "%firstArg%"=="-xmltotext" goto notXmlToText
set fb_mainclass=edu.umd.cs.findbugs.PrintingBugReporter
goto shift1
:notXmlToText

:: copyBuggySource
if not "%firstArg%"=="-copyBS" goto notCopyBS
set fb_mainclass=edu.umd.cs.findbugs.workflow.CopyBuggySource
goto shift1
:notCopyBS

:: defectDensity
if not "%firstArg%"=="-defectDensity" goto notDefectDensity
set fb_mainclass=edu.umd.cs.findbugs.workflow.DefectDensity
goto shift1
:notDefectDensity

:: filterBugs
if not "%firstArg%"=="-filterBugs" goto notFilterBugs
set fb_mainclass=edu.umd.cs.findbugs.workflow.Filter
goto shift1
:notFilterBugs

:: listBugDatabaseInfo
if not "%firstArg%"=="-listBugDatabaseInfo" goto notListBugDatabaseInfo
set fb_mainclass=edu.umd.cs.findbugs.workflow.ListBugDatabaseInfo
goto shift1
:notListBugDatabaseInfo

:: mineBugHistory
if not "%firstArg%"=="-mineBugHistory" goto notMineBugHistory
set fb_mainclass=edu.umd.cs.findbugs.workflow.MineBugHistory
goto shift1
:notMineBugHistory

:: printAppVersion
if not "%firstArg%"=="-printAppVersion" goto notPrintAppVersion
set fb_mainclass=edu.umd.cs.findbugs.workflow.PrintAppVersion
goto shift1
:notPrintAppVersion

:: printClass
if not "%firstArg%"=="-printClass" goto notPrintClass
set fb_mainclass=edu.umd.cs.findbugs.workflow.PrintClass
goto shift1
:notPrintClass

:: rejarForAnalysis
if not "%firstArg%"=="-rejar" goto notRejar
set fb_mainclass=edu.umd.cs.findbugs.workflow.RejarClassesForAnalysis
goto shift1
:notRejar

:: setBugDatabaseInfo
if not "%firstArg%"=="-setInfo" goto notSetBugDatabaseInfo
set fb_mainclass=edu.umd.cs.findbugs.workflow.SetBugDatabaseInfo
goto shift1
:notSetBugDatabaseInfo

:: unionBugs
if not "%firstArg%"=="-unionBugs" goto notUnionBugs
set fb_mainclass=edu.umd.cs.findbugs.workflow.UnionResults
goto shift1
:notUnionBugs

:: xpathFind
if not "%firstArg%"=="-xpathFind" goto notXPathFind
set fb_mainclass=edu.umd.cs.findbugs.workflow.XPathFind
goto shift1
:notXPathFind

if not "%firstArg%"=="-gui" goto notGui
set launchUI=2
set launcher=javaw.exe
goto shift1
:notGui

if not "%firstArg%"=="-gui1" goto notGui1
set launchUI=1
set javaProps=-Dfindbugs.launchUI=1 %javaProps%
set launcher=javaw.exe
goto shift1
:notGui1

if not "%firstArg%"=="-textui" goto notTextui
set launchUI=0
set launcher=java.exe
set start=
goto shift1
:notTextui

if not "%firstArg%"=="-debug" goto notDebug
set launcher=java.exe
set start=
set debugArg=-Dfindbugs.debug=true
goto shift1
:notDebug

if not "%firstArg%"=="-help" goto notHelp
set launchUI=help
set launcher=java.exe
set start=
goto shift1
:notHelp

if not "%firstArg%"=="-version" goto notVersion
set launchUI=version
set launcher=java.exe
set start=
goto shift1
:notVersion

if "%firstArg%"=="-home" set FINDBUGS_HOME=%secondArg%
if "%firstArg%"=="-home" goto shift2

if "%firstArg%"=="-jvmArgs" set jvmargs=%secondArg%
if "%firstArg%"=="-jvmArgs" goto shift2

if "%firstArg%"=="-maxHeap" set maxheap=%secondArg%
if "%firstArg%"=="-maxHeap" goto shift2

if "%firstArg%"=="-conserveSpace" set conserveSpaceArg=-Dfindbugs.conserveSpace=true
if "%firstArg%"=="-conserveSpace" goto shift1

if "%firstArg%"=="-workHard" set workHardArg=-Dfindbugs.workHard=true
if "%firstArg%"=="-workHard" goto shift1

if "%firstArg%"=="-javahome" set javahome=%secondArg%\bin\
if "%firstArg%"=="-javahome" goto shift2

if "%firstArg%"=="-property" set javaProps=-D%secondArg% %javaProps%
if "%firstArg%"=="-property" goto shift2

if "%firstArg%"=="" goto launch

set args=%args% "%firstArg%"
goto shift1

:: ----------------------------------------------------------------------
:: Launch FindBugs
:: ----------------------------------------------------------------------
:launch
:: Make sure FINDBUGS_HOME is set.
:: If it isn't, try using the default value based on the
:: directory path of the invoked script.
:: Note that this will fail miserably if the value of FINDBUGS_HOME
:: has quote characters in it.
if not "%FINDBUGS_HOME%"=="" goto checkHomeValid
set FINDBUGS_HOME=%default_findbugs_home%

:checkHomeValid
if not exist "%FINDBUGS_HOME%\lib\%appjar%" goto homeNotSet

:found_home
:: Launch FindBugs!
if "%fb_mainclass%"=="" goto runJar
"%javahome%%launcher%" %debugArg% %conserveSpaceArg% %workHardArg% %javaProps% "-Dfindbugs.home=%FINDBUGS_HOME%" -Xmx%maxheap%m %jvmargs% "-Dfindbugs.launchUI=%launchUI%" -cp "%FINDBUGS_HOME%\lib\%appjar%" %fb_mainclass% %args%
goto end
:runjar
%start% "%javahome%%launcher%" %debugArg% %conserveSpaceArg% %workHardArg% %javaProps% "-Dfindbugs.home=%FINDBUGS_HOME%" -Xmx%maxheap%m %jvmargs% "-Dfindbugs.launchUI=%launchUI%" -jar "%FINDBUGS_HOME%\lib\%appjar%" %args%
goto end

:: ----------------------------------------------------------------------
:: Report that FINDBUGS_HOME is not set (and was not specified)
:: ----------------------------------------------------------------------
:homeNotSet
echo Could not find FindBugs home directory.  There may be a problem
echo with the FindBugs installation.  Try setting FINDBUGS_HOME, or
echo re-installing.
goto end

:end
�[zB+�Y��>����-/�������a�bOd�(W��^�z�x�vt�%�U�;�[��*���2�<'� խ����AB�1�>���kB���4�Ri$-<
Gy�"bg�g�S�s�R^΂-��i@1I=��&�����������%L��7v�{���H7�8x'`�,���B!����EHg��v�qT	��S��waշ�D?bb��b�v��W8(	��X�UN�H����D�f�+T����i�#C�W��YJ��_����O�0aI����5�<��a�.��c/=/�v����ٔ���U\�q �l=GM]��me1@�(`ߔ!��	/����z��qXT��z���ƫ����c�z�'��a��Np���c�el��sWȟÕ8c�kX3�K��QPa��*j�������C��F@�x�y��$�����#��p���*ل#s����H,��p��냛��%f.=�V>*dE���$Wꦕ���HkF���MF�$"��o�F'��eKA�ﳘ��>l�~��$2����X"�|�K�ò�A���8�|5Wy���nOS�K����6�!����ͻ�#��iW`�j����y���R�����u�jh�~��js;p��d�h<�X=����3��d��c����N������ofN�ٴU;����w�+�o$�K�m������l������\k����s*�����{ޖ�f,��k�dNQ�J�C �y���~�M݅>��Rr�سf/:u��3�)��y�.�_����HR�"Lܠ���]���.��Q���� ������ISI���Ƚ�
���R��Ђ��I7��ݩG�A���o�<6�p����"����4��~;M������R�xd�R[���!�/@���}ؼ�L�K�s�p�Y.8sH�%ji��굗��ۧ�y_t�4	��:����� ���:�!{�����5ût*���<v_�d!W�AA�����åv����r�w��Y�