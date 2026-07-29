<#--
  #%L
  License Maven Plugin
  %%
  Copyright (C) 2012 Codehaus, Tony Chemit
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.

  You should have received a copy of the GNU General Lesser Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-3.0.html>.
  #L%
  -->
<#-- To render the third-party file.
 Available context :

 - dependencyMap a collection of Map.Entry with
   key are dependencies (as a MavenProject) (from the maven project)
   values are licenses of each dependency (array of string)

 - licenseMap a collection of Map.Entry with
   key are licenses of each dependency (array of string)
   values are all dependencies using this license
-->
<form>
<p><span color="header" font="header">JVerein</span>
<br/>Vereinsverwaltung-Plugin fuer Jameica
<br/>https://openjverein.github.io
<br/>GPL 3 http://www.gnu.org/licenses/LICENSE-3.0.txt
</p>
<p><span color="header" font="header">Verwendete Komponenten</span></p>
<#if dependencyMap?size == 0>
<p>Keine Bibliotheken gefunden</p>
<#else>
    <#list dependencyMap as e>
        <#assign project = e.getKey()/>
        <#-- bsh brauch extrabehandlung, da es nicht von maven stammt -->
        <#if project.name == "bsh">
         <p><b>${project.name}(${project.version})</b>
         <br/>BeanShell - Simple Java Scripting
         <br/>${project.groupId} : ${project.artifactId}
         <br/>https://github.com/beanshell/beanshell/
         <br/>Apache-2.0 license - http://www.apache.org/licenses/LICENSE-2.0.txt
         </p>
        <#else>
         <p><b>${project.name}(${project.version})</b>
         <br/>${project.description}
         <br/>${project.groupId} : ${project.artifactId}
         <br/>${(project.url!"")}
         <#list project.licenses as l>
         	<br/>${l.name} - ${(l.url!"")}
         </#list>
         </p>
         </#if>
    </#list>
</#if>
<p><br/></p>
</form>