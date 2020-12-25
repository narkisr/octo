# Intro

Backup your Github repos (because pushing code online isn't good [enough](https://about.gitlab.com/2017/02/10/postmortem-of-database-outage-of-january-31/)).

[![Build Status](https://travis-ci.org/narkisr/octo.png)](https://travis-ci.org/narkisr/octo)

# Usage

Make sure to have an octo.edn file and an ssh access key from the current user account:

```bash
 $ octo sync octo.edn
 ...
 # Once done the git bundles are under (per user/org)
 $ ls ~/workspace/repos/narkisr/bundles
 aptly-docker.bundle
 basebox-packer.bundle

 # push to a remote backup like s3 using zbackup and rclone
 $ octo push octo.edn

 # restore backup from remote backup
 $ octo pull octo.edn
```

## Configuration

The format of octo.edn is:

```clojure
{
 :workspace "/home/ronen/workspace"
 :user "GITHUB USER"
 :token "PERSONAL ACCESS TOKEN"
 :repos [
    {:user "narkisr"
     :options {:fpm-barbecue {:branch "master"}} ; only backup a single branch
     :exclude []
     :layouts [["elm-*" "narkisr/elm"] [".*" "narkisr"]]
    }
    {:org "opskeleton"
     :exclude []
     :layouts [[".*" "opskeleton"]]
    }
  ]

 :push {
   :zbackup {
     :password-file ""
    }

   :rclone {
    :dest ""
   }
 }

}
```

Glossary:

* workspace: backup destination folder.
* user: github user.
* token: a personal access token.
* repos a collection of users/orgs we want to backup:
  * user/org: the user/org name that is backed up.
  * options: specific repo options (currently only selecting a single branch to back up).
  * exclude: which repos not to back up.
  * layouts: mapping from folder name regex match into destination folder,
    for example match all the repos with name elm-* prefix into narkisr/elm folder.
* push:
  * zbackup.password-file: password for zbackup (if using push/pull)
  * rclone.dest: A remote backup address

## Backup lifecycle

Each repo:

1. Cloned into a bare repo using 'git clone --mirror'.
2. Exported to a single file using 'git bundle create'.
3. Incremented using 'git fetch remote'.

# Install

Perquisites:

* JRE 1.8
* Git binary.
* [rclone](rclone.org) and [zbackup](zbackup.org) (if using push/pull).
* Ubuntu (Should work on any Linux system but not tested).

```bash
$ wget https://github.com/narkisr/octo/releases/download/0.8.2/octo
$ sudo mv octo /usr/local/bin
```

# Copyright and license

Copyright [2020] [Ronen Narkis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
