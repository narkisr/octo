import Effects exposing (Never)
import StartApp
import Task
import Signal exposing (map, filter)
import Application exposing (init, view, update)

import Time exposing (every, second)

app =
  StartApp.start
    { init = init
    , update = update
    , view = view
    , inputs = []
    }

main =
  app.html 


port tasks : Signal (Task.Task Never ())
port tasks =
  app.tasks

