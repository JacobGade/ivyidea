# ivyidea

Fork of the project created by Guy Mahieu. 

Optimized for projects with a lot of modules, and with extra feedback regarding progress.

<strong>1.0.19</strong>
<ul>
    <li>Updated plugin to be compatible with IntelliJ 2021.1</li>
</ul>
<strong>1.0.18</strong>
<ul>
    <li>Reverted changes to internal logging that caused intermittent NPE on latest version of IntelliJ.</li>
</ul>
<strong>1.0.17</strong>
<ul>
    <li>Disable indexing while updating IntelliJ module dependencies.</li>
</ul>
<strong>1.0.16</strong>
<ul>
    <li>Added log statement before starting resolution to make it more apparent where errors belong.</li>
    <li>Continue resolving all modules even when dependency error is encountered in one module.</li>
    <li>Summary of errors will be displayed after resolution is done.</li>
    <li>Allow the console to be visible during indexing.</li>
</ul>
<strong>1.0.15</strong>
<ul>
    <li>Improve feedback during resolution. Progress will not be reflected both in the console and in the indicator at the bottom.</li>
    <li>Allow for relative paths for settings files if they are inside the project folder</li>
    <li>Attempt to match Ivy configuration name with intelliJ dependency scope. (Beware of difference in transitivity. In IntelliJ test dependency includes the other projects test dependencies)</li>
    <li>When resolving all modules only resolve for loaded modules</li>
    <li>Better resilience towards missing ivy files, so it is properly reported.</li>
    <li>Updating IntelliJ project dependencies is no longer done in parallel to avoid issues on projects with many modules.</li>
    <li>Reuse ivy instance when settings are identical to improve performance</li>
    <li>Refactored some of the resolution code to make reuse possible.</li>
</ul>

<strong>Forked</strong>

<strong>1.0.14</strong>
<ul>
    <li>When trying to resolve dependencies without an Ivy settings file, an IllegalArgumentException 
        was thrown when clicking on the 'Open Project Settings' link</li>
</ul>
<strong>1.0.13</strong>
<ul>
    <li>The IvyIDEA settings window was no longer visible in IntelliJ 2016.1</li>
</ul>
<strong>1.0.12</strong>
<ul>
    <li>The configurations to resolve are now stored alphabetically in the .iml file</li>
    <li>Modified files are now saved before starting to resolve the dependencies</li>
</ul>
<strong>1.0.11</strong>
<ul>
    <li>Fixed compatibility issue with IntelliJ 11</li>
    <li>Upgraded internal Apache Ivy to 2.4.0 (including dependencies)</li>
</ul>
<strong>1.0.10</strong>
<ul>
    <li>Fixed compatibility issue with IntelliJ 13.1</li>
</ul>
<strong>1.0.9</strong>
<ul>
    <li>Added an extra configuration option to always attach the source/javadoc artifacts, even when
        they aren't selected by an Ivy configuration.</li>
    <li>Fixed compatibility issue with IntelliJ 13.1</li>
    <li>Upgraded internal Apache Ivy to 2.4.0-rc1 (including dependencies)</li>
</ul>
<strong>1.0.8</strong>
<ul>
    <li>Fixed compatibility issue with IntelliJ 13.0.2 EAP (build 133.609)</li>
</ul>
<strong>1.0.7</strong>
<ul>
    <li>Fixed compatibility issue with IntelliJ 13</li>
    <li>Fixed issue with resolving dependencies of a single module if another module had an incorrect ivy file</li>
</ul>
<strong>1.0.6</strong>
<ul>
    <li>Fixed issue with the 'Only resolve specific ivy configurations'-option on the IvyIDEA facet page</li>
    <li>Fixed compatibility issue with IntelliJ 13 beta1 (build 132.947)</li>
</ul>
<strong>1.0.5</strong>
<ul>
    <li>Improved resolving dependencies in the background</li>
    <li>Added an extra configuration option to always resolve in the background</li>
    <li>Resolving dependencies can now be canceled</li>
    <li>Fixed problem parsing ivy.xml files extending others</li>
    <li>Fixed compatibility issue with IntelliJ 13 EAP (build 130.1619)</li>
</ul>
<strong>1.0.4</strong>
<ul>
    <li>Upgraded internal Apache Ivy to 2.3.0 (including dependencies)</li>
    <li>Added some extra resolve options to the project settings</li>
</ul>
<strong>1.0.3</strong>
<ul>
    <li>Upgraded internal Apache Ivy to 2.3.0-rc2 (including dependencies)</li>
</ul>
<strong>1.0.2</strong>
<ul>
    <li>Upgraded internal Apache Ivy to 2.3.0-rc1 (including dependencies)</li>
    <li>Fixed issue when loading properties files containing cyclic properties</li>
</ul>
<strong>1.0.1</strong>
<ul>
    <li>Bugfix: it was not possible to use the default Ivy settings</li>
</ul>
<strong>1.0</strong>
<ul>
    <li>Added support for 'mar' artifact types (Axis module archives)</li>
    <li>Fixed compatibility issues with IntelliJ 11</li>
    <li>Fixed problem on Windows when the case of the ivy-cache path didn't match the case on disk</li>
</ul>
<strong>0.9</strong>
<ul>
    <li>Upgraded internal Apache Ivy to 2.2.0 (including dependencies)</li>
    <li>Support for using ${} style properties in ivy and ivysettings files</li>
    <li>Improved lookup method for artifacts; now useOrigin="true" will also be supported.</li>
    <li>Resolved config names are now listed in the IvyIDEA console</li>
    <li>Resolved library names can now contain the module and or configuration name (with help from wajiii).</li>
    <li>Configurable log level for ivy logging (with help from wajiii)</li>
    <li>Dependencies are now added to the module library with a relative path</li>
    <li>The types used for classes/sources/javadoc artifacts is now configurable</li>
    <li>Several small fixes and improvements</li>
</ul>
<strong>0.8</strong>
<ul>
    <li>Upgraded internal ivy to 2.0.0rc2</li>
    <li>Improved exception handling</li>
</ul>
<strong>0.7-alpha</strong>
<ul>
    <li>IvyIDEA is now compatible with IntelliJ 8.0 (and will run on previous versions as well)</li>
    <li>Switched to JDK 1.5 so the plugin will also run on mac os</li>
    <li>Made looking up intellij module dependencies more lenient; now the revision is ignored when
        identifying dependencies as existing intellij modules rather than jars</li>
</ul>
        