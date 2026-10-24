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
    subgraph :feature:news
      direction TB
      :feature:news:api[api]:::android-library
      :feature:news:impl[impl]:::android-feature
    end
    subgraph :feature:transfers
      direction TB
      :feature:transfers:api[api]:::android-library
      :feature:transfers:impl[impl]:::android-feature
    end
    subgraph :feature:bookmarks
      direction TB
      :feature:bookmarks:api[api]:::android-library
      :feature:bookmarks:impl[impl]:::android-feature
    end
    subgraph :feature:details
      direction TB
      :feature:details:api[api]:::android-library
      :feature:details:impl[impl]:::android-feature
    end
    subgraph :feature:search
      direction TB
      :feature:search:api[api]:::android-library
      :feature:search:impl[impl]:::android-feature
    end
    subgraph :feature:details-transfers
      direction TB
      :feature:details-transfers:api[api]:::android-library
      :feature:details-transfers:impl[impl]:::android-feature
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
  :app -.-> :core:notifications
  :app -.-> :core:ui
  :app -.-> :feature:bookmarks:api
  :app -.-> :feature:bookmarks:impl
  :app -.-> :feature:details-transfers:api
  :app -.-> :feature:details-transfers:impl
  :app -.-> :feature:details:api
  :app -.-> :feature:details:impl
  :app -.-> :feature:news:api
  :app -.-> :feature:news:impl
  :app -.-> :feature:search:api
  :app -.-> :feature:search:impl
  :app -.-> :feature:settings:impl
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
  :feature:bookmarks:impl -.-> :core:data
  :feature:bookmarks:impl -.-> :core:designsystem
  :feature:bookmarks:impl -.-> :core:ui
  :feature:bookmarks:impl -.-> :feature:bookmarks:api
  :feature:details-transfers:impl -.-> :core:data
  :feature:details-transfers:impl -.-> :core:designsystem
  :feature:details-transfers:impl -.-> :core:ui
  :feature:details-transfers:impl -.-> :feature:details-transfers:api
  :feature:details:impl -.-> :core:data
  :feature:details:impl -.-> :core:designsystem
  :feature:details:impl -.-> :core:ui
  :feature:details:impl -.-> :feature:details:api
  :feature:news:impl -.-> :core:data
  :feature:news:impl -.-> :core:designsystem
  :feature:news:impl -.-> :core:domain
  :feature:news:impl -.-> :core:notifications
  :feature:news:impl -.-> :core:ui
  :feature:news:impl -.-> :feature:news:api
  :feature:search:impl -.-> :core:data
  :feature:search:impl -.-> :core:designsystem
  :feature:search:impl -.-> :core:domain
  :feature:search:impl -.-> :core:ui
  :feature:search:impl -.-> :feature:search:api
  :feature:settings:impl -.-> :core:data
  :feature:settings:impl -.-> :core:designsystem
  :feature:settings:impl -.-> :core:ui
  :feature:transfers:impl -.-> :core:data
  :feature:transfers:impl -.-> :core:designsystem
  :feature:transfers:impl -.-> :core:domain
  :feature:transfers:impl -.-> :core:notifications
  :feature:transfers:impl -.-> :core:ui
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
