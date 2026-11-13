# `:benchmarks`

## Module dependency graph

<!--region graph-->
```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
  subgraph :feature
    direction TB
    subgraph :feature:settings
      direction TB
      :feature:settings:impl[impl]:::android-feature
    end
    subgraph :feature:articles
      direction TB
      :feature:articles:api[api]:::android-library
      :feature:articles:impl[impl]:::android-feature
    end
    subgraph :feature:leagues
      direction TB
      :feature:leagues:api[api]:::android-library
      :feature:leagues:impl[impl]:::android-feature
    end
    subgraph :feature:bookmarks
      direction TB
      :feature:bookmarks:api[api]:::android-library
      :feature:bookmarks:impl[impl]:::android-feature
    end
    subgraph :feature:transfers
      direction TB
      :feature:transfers:api[api]:::android-library
      :feature:transfers:impl[impl]:::android-feature
    end
    subgraph :feature:rumours
      direction TB
      :feature:rumours:api[api]:::android-library
      :feature:rumours:impl[impl]:::android-feature
    end
    subgraph :feature:search
      direction TB
      :feature:search:api[api]:::android-library
      :feature:search:impl[impl]:::android-feature
    end
    subgraph :feature:article-details
      direction TB
      :feature:article-details:api[api]:::android-library
      :feature:article-details:impl[impl]:::android-feature
    end
    subgraph :feature:transfer-details
      direction TB
      :feature:transfer-details:api[api]:::android-library
      :feature:transfer-details:impl[impl]:::android-feature
    end
  end
  subgraph :sync
    direction TB
    :sync:work[work]:::android-library
  end
  subgraph :core
    direction TB
    :core:analytics[analytics]:::android-library
    :core:common[common]:::android-library
    :core:data[data]:::android-library
    :core:database[database]:::android-library
    :core:datastore[datastore]:::android-library
    :core:datastore-proto[datastore-proto]:::jvm-library
    :core:designsystem[designsystem]:::android-library
    :core:domain[domain]:::android-library
    :core:model[model]:::jvm-library
    :core:navigation[navigation]:::android-library
    :core:network[network]:::android-library
    :core:notifications[notifications]:::android-library
    :core:ui[ui]:::android-library
  end
  :benchmarks[benchmarks]:::android-test
  :app[app]:::android-application

  :app -.->|baselineProfile| :benchmarks
  :app -.-> :core:analytics
  :app -.-> :core:common
  :app -.-> :core:data
  :app -.-> :core:designsystem
  :app -.-> :core:model
  :app -.-> :core:navigation
  :app -.-> :core:notifications
  :app -.-> :core:ui
  :app -.-> :feature:article-details:api
  :app -.-> :feature:article-details:impl
  :app -.-> :feature:articles:api
  :app -.-> :feature:articles:impl
  :app -.-> :feature:bookmarks:api
  :app -.-> :feature:bookmarks:impl
  :app -.-> :feature:leagues:api
  :app -.-> :feature:leagues:impl
  :app -.-> :feature:rumours:api
  :app -.-> :feature:rumours:impl
  :app -.-> :feature:search:api
  :app -.-> :feature:search:impl
  :app -.-> :feature:settings:impl
  :app -.-> :feature:transfer-details:api
  :app -.-> :feature:transfer-details:impl
  :app -.-> :feature:transfers:api
  :app -.-> :feature:transfers:impl
  :app -.-> :sync:work
  :benchmarks -.->|testedApks| :app
  :core:data -.-> :core:analytics
  :core:data --> :core:common
  :core:data --> :core:database
  :core:data --> :core:datastore
  :core:data --> :core:network
  :core:data -.-> :core:notifications
  :core:database --> :core:model
  :core:datastore -.-> :core:common
  :core:datastore --> :core:datastore-proto
  :core:datastore --> :core:model
  :core:domain --> :core:data
  :core:domain --> :core:model
  :core:network --> :core:common
  :core:network --> :core:model
  :core:notifications -.-> :core:common
  :core:notifications --> :core:model
  :core:ui --> :core:analytics
  :core:ui --> :core:designsystem
  :core:ui --> :core:model
  :feature:article-details:api --> :core:navigation
  :feature:article-details:impl -.-> :core:data
  :feature:article-details:impl -.-> :core:designsystem
  :feature:article-details:impl -.-> :core:ui
  :feature:article-details:impl -.-> :feature:article-details:api
  :feature:article-details:impl -.-> :feature:search:api
  :feature:articles:api --> :core:navigation
  :feature:articles:impl -.-> :core:data
  :feature:articles:impl -.-> :core:designsystem
  :feature:articles:impl -.-> :core:domain
  :feature:articles:impl -.-> :core:notifications
  :feature:articles:impl -.-> :core:ui
  :feature:articles:impl -.-> :feature:article-details:api
  :feature:articles:impl -.-> :feature:articles:api
  :feature:articles:impl -.-> :feature:rumours:api
  :feature:articles:impl -.-> :feature:transfer-details:api
  :feature:articles:impl -.-> :feature:transfers:api
  :feature:bookmarks:api --> :core:navigation
  :feature:bookmarks:impl -.-> :core:data
  :feature:bookmarks:impl -.-> :core:designsystem
  :feature:bookmarks:impl -.-> :core:ui
  :feature:bookmarks:impl -.-> :feature:article-details:api
  :feature:bookmarks:impl -.-> :feature:bookmarks:api
  :feature:bookmarks:impl -.-> :feature:search:api
  :feature:bookmarks:impl -.-> :feature:transfer-details:api
  :feature:leagues:api --> :core:navigation
  :feature:leagues:impl -.-> :core:designsystem
  :feature:leagues:impl -.-> :core:ui
  :feature:leagues:impl -.-> :feature:leagues:api
  :feature:rumours:api --> :core:navigation
  :feature:rumours:impl -.-> :core:designsystem
  :feature:rumours:impl -.-> :core:ui
  :feature:rumours:impl -.-> :feature:rumours:api
  :feature:search:api --> :core:navigation
  :feature:search:impl -.-> :core:data
  :feature:search:impl -.-> :core:designsystem
  :feature:search:impl -.-> :core:domain
  :feature:search:impl -.-> :core:ui
  :feature:search:impl -.-> :feature:article-details:api
  :feature:search:impl -.-> :feature:search:api
  :feature:search:impl -.-> :feature:transfer-details:api
  :feature:settings:impl -.-> :core:data
  :feature:settings:impl -.-> :core:designsystem
  :feature:settings:impl -.-> :core:ui
  :feature:transfer-details:api --> :core:navigation
  :feature:transfer-details:impl -.-> :core:data
  :feature:transfer-details:impl -.-> :core:designsystem
  :feature:transfer-details:impl -.-> :core:ui
  :feature:transfer-details:impl -.-> :feature:transfer-details:api
  :feature:transfers:api --> :core:navigation
  :feature:transfers:impl -.-> :core:data
  :feature:transfers:impl -.-> :core:designsystem
  :feature:transfers:impl -.-> :core:domain
  :feature:transfers:impl -.-> :core:notifications
  :feature:transfers:impl -.-> :core:ui
  :feature:transfers:impl -.-> :feature:transfer-details:api
  :feature:transfers:impl -.-> :feature:transfers:api
  :sync:work -.-> :core:analytics
  :sync:work -.-> :core:data
  :sync:work -.-> :core:notifications

classDef android-application fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef android-feature fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-test fill:#A0C4FF,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```

<details><summary>📋 Graph legend</summary>

```mermaid
graph TB
  application[application]:::android-application
  feature[feature]:::android-feature
  library[library]:::android-library
  jvm[jvm]:::jvm-library

  application -.-> feature
  library --> jvm

classDef android-application fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef android-feature fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-test fill:#A0C4FF,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```

</details>
<!--endregion-->
