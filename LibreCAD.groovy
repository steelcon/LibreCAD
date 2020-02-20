def LibreCadExePath()
{
	return "%WORKSPACE%\\LibreCAD.exe"
}
def Build()
{
	bat "qmake libreCAD.pro -tp vc -r"
	bat "msbuild librecad.sln  /p:Platform=Win32 /p:Configuration=Release"
}
def GetBuiltFiles()
{
	File myObj = new File("$WORKSPACE\\windows\\"); 
	String[] files = myObj.list();
	String fileNames = ""
	int size = files.length;
	for (int i=0; i<size; i++)
	{
		fileNames = fileNames + ","
	}
	
	if(fileNames.isEmpty())
		return fileNames
	
	return fileNames.substring(0, fileNames.length()-1)
}
def BuildInstaller()
{
	bat "mkdir %WORKSPACE%\\generated\\"
	writeFile file: GetInstallerPath(), text: "something"
}
def GetInstallerPath()
{
	return "$WORKSPACE\\generated\\LibreCAD-Installer.exe"
}

return this