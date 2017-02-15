# Intro

Backup your Github repos ([Gitlab](https://about.gitlab.com/2017/02/10/postmortem-of-database-outage-of-january-31/)).

# Usage

```bash 
 $ octo octo.edn
```

## Configuration
octo.edn describes the users/orgs to backup and options for each:

{
 :workspace "/home/ronen/workspace"
 :repos [
    {:user "narkisr"
     :options {:fpm-barbecue {:branch "master"}} ; only backup a single branch
     :exclude []
     :layouts [[".*" "narkisr"]]
    }
    {:user "opskeleton"
     :exclude []
     :layouts [[".*" "opskeleton"]]
    }
    {:user "celestial-ops"
     :exclude []
     :layouts [[".*" "celestial"]]
    }
    {:user "pulling-strings"
     :exclude []
     :layouts [[".*" "strings"]]
    }
  ]
}

Where: 

* workspace: backup destination folder
* repos:  a collection of users/orgs we want to backup:
  * options: specific repo options (like selecting a single branch to back up).
  * exclude: which repos not to back up.
  * layouts: mapping of the folder structure of backups.
 
## Backup lifecycle

Each repo:

1. Cloned into a bare repo using 'git clone --mirror'.
2. Exported to a single file using 'git bundle create'.
3. Incremented using 'git remote update --prune' if it already clone.

## TBD

* Support private repositories.

# Copyright and license

Copyright [2017] [Ronen Narkis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
