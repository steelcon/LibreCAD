pipeline 
{
	agent any
	stages 
	{
		stage('Checkout') 
		{
		  steps 
		  {
			bat 'SET'
			deleteDir()
			checkout scm
			stash name: 'source'
		  }
		}
		stage('Build') 
		{
			agent 
			{
				label 'librecad'
			}
			steps 
			{
				bat 'SET'
				deleteDir()
				unstash 'source'
				script
				{
					def LibreCAD = load 'LibreCAD.groovy'
					LibreCAD.Build()
					stash includes: '/**/*.exe', name: 'build_files'
				}
			}
			post
			{
				always
				{
					archiveArtifacts allowEmptyArchive: true, artifacts: '/**/*.exe'
				}
			}
		}
	
		stage('Deploy') 
		{
			agent 
			{
				label 'librecad'
			}
			steps 
			{
				bat 'SET'
				deleteDir()
				// build installer
				unstash 'source'
				unstash 'build_files'
				script
				{
					// copy installer to prod rel
					def LibreCAD = load 'LibreCAD.groovy'
					bat script: 'xcopy "\\\\cam-file\\devcommon\\Jenkins\\Binary Management\\VisualC++\\vc_redist.x86.exe" "%WORKSPACE%/redist" /y'
					LibreCAD.BuildInstaller()
					def
					    networkPath = CreateNetworkPathForInstaller()
					
					bat script: 'xcopy "' +LibreCAD.GetInstallerPath()+ '" "' + networkPath + '" /y'
							
					// create installer link file
					CreateInstallerLinksFile("InstallerLinksLibreCAD.txt", networkPath + "LibreCAD-Installer.exe","")
					stash includes: '/**/InstallerLinks*.txt', name: 'installer'
				}
			}
		}
		stage('Email') 
		{
			steps 
			{
				bat 'SET'
				deleteDir()
				unstash 'source'
				script 
				{
					def notify = load 'notify.groovy'
					if (!env.INSTALLER_TYPE || (env.INSTALLER_TYPE == '')) 
					{
						notify.sendSuccessEmail(false)
					} 
					else 
					{
						unstash 'installer'
						notify.sendSuccessEmail(true)
					}          
				}
			}
			post 
			{	
				always 
				{
					script 
					{
						if (mustCleanWorkspace()) 
						{
							deleteDir()
						}
					}
				}
		  
			}
        }
	}
	post 
	{
	    failure 
		{
			emailext(subject: "${env.PRODUCT_NAME} ${env.VERSION_FULL} - Failure!",
				body: """<p>Check the results for <a href='${currentBuild.absoluteUrl}'>Build #${currentBuild.number}</a>.</p>""",
				mimeType: 'text/html',
				recipientProviders: [[$class: 'DevelopersRecipientProvider']])   
		}
	}
	environment 
	{
		PRODUCT_NAME = 'LibreCAD for ProNest'
		VERSION_BUILD = getVersionBuild()
		INSTALLER_TYPE = getInstallerType()
		RECIPIENTS = 'mtcprogramming, steven.bertken, chris.pollard'
		VERSION_FULL = "2.2.0.${VERSION_BUILD}"
		TARGET_PLATFORM = '32-bit'
		BUILD_DISPLAY_NAME = getDisplayName()
	}
	options 
	{
		buildDiscarder(logRotator(numToKeepStr: '30'))
	}
}
def mustCleanWorkspace()
{
	return false
}

def getVersionBuild()  
{    
  return new Date() - new Date("1/1/2000")
}

def getDisplayName()
{
	def
		displayName = getInstallerType()
	if(displayName != "")
		return displayName
		
	return "(bootleg)"
}
def getInstallerType() 
{
	def 
		branchName = env.BRANCH_NAME.toLowerCase()
		
	if (branchName == "master") 
		return "Release"
	else 
		return "NR"
}
def CreateInstallerLinksFile(linksFileName, pathToInstaller, version)
{
	def body = "<p><a href='" + pathToInstaller + "'>LibreCAD" +  version + "</a>"
	writeFile file: linksFileName, text: body
}
def CreateNetworkPathForInstaller()
{
	def 
		installerType = getInstallerType() 
	if( installerType == 'Release')
		installerType = ''
	else if( installerType == '' || installerType == null)
		installerType = ' NR '
	else
		installerType = ' ' + installerType + ' '
	
	return  '\\\\cam-issvr\\installations\\built by jenkins\\LibreCAD' + installerType +' (' + env.TARGET_PLATFORM + ')\\' + env.BRANCH_NAME + '\\' + "${currentBuild.number}" + '\\'
}