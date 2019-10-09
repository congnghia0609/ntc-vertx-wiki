<#include "header.ftl">

<div class="row">

  <div class="col-md-12 mt-1">
  <#if canCreatePage>
    <div class="float-right">
      <form class="form-inline" action="/action/create" method="post">
        <div class="form-group">
          <input type="text" class="form-control" id="name" name="name" placeholder="New page name">
        </div>
        <button type="submit" class="btn btn-primary">Create</button>
      </form>
    </div>
  </#if>
    <h1 class="display-4">${title}</h1>
    <div class="float-right">
      <a class="btn btn-outline-danger" href="/logout" role="button" aria-pressed="true">Logout (${username})</a>
    </div>
  </div>

  <div class="col-md-12 mt-1">
  <#list pages>
    <h2>Pages:</h2>
    <ul>
      <#items as page>
        <li><a href="/wiki/${page}">${page}</a></li>
      </#items>
    </ul>
  <#else>
    <p>The wiki is currently empty!</p>
  </#list>

  <#if canCreatePage>
    <#if backup_gist_url?has_content>
      <div class="alert alert-success" role="alert">
        Successfully created a backup:
        <a href="${backup_gist_url}" class="alert-link">${backup_gist_url}</a>
      </div>
    <#else>
      <p>
        <a class="btn btn-outline-secondary btn-sm" href="/action/backup" role="button" aria-pressed="true">Backup</a>
      </p>
    </#if>
  </#if>
  </div>

</div>

<#include "footer.ftl">