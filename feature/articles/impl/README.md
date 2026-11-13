# `:feature:news`

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
    subgraph :feature:articles
      direction TB
      :feature:articles:api[api]:::android-library
      :feature:articles:impl[impl]:::android-feature
    end
    subgraph :feature:article-details
      direction TB
      :feature:article-details:api[api]:::android-library
    end
    subgraph :feature:rumours
      direction TB
      :feature:rumours:api[api]:::android-library
    end
    subgraph :feature:transfer-details
      direction TB
      :feature:transfer-details:api[api]:::android-library
    end
    subgraph :feature:transfers
      direction TB
      :feature:transfers:api[api]:::android-library
    end
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
  :feature:rumours:api --> :core:navigation
  :feature:transfer-details:api --> :core:navigation
  :feature:transfers:api --> :core:navigation

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
