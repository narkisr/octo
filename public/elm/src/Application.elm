module Application where

import Html exposing (..)
import Html.Attributes exposing (type', class, id, style)
import Effects exposing (Effects, Never, batch, map)
import Set
import Http exposing (Error(BadResponse))
import Task
import Json.Decode as Json exposing (..)
import Nav.Side as NavSide exposing (Active(Repos), Section(List, Backup))

import Debug

init : (Model, Effects Action)
init =
    (Model [], getRepos)

type alias Repository = 
 {url : String, name : String, statusChanged : Bool} 

type alias Model = 
  { repos : List Repository }

type Action = 
   Load (Result Http.Error (List Repository))

update : Action ->  Model-> (Model , Effects Action)
update action model =
  case action of 
    Load result -> 
      case result of
         Result.Ok repos -> 
           ({ model | repos = repos }, Effects.none)
         Result.Err e -> 
           case e of 
              _ -> 
               Debug.log (toString e) (model , Effects.none)

partition n list =
  let
    catch = (List.take n list)
  in 
    if n == (List.length catch) then
      [catch] ++ (partition n (List.drop n list))
    else
      [catch]

repoPanel : Repository -> Html
repoPanel repo =
  let
    rgb = if repo.statusChanged then "red" else "green"
  in 
    div [class "col-md-4 col-xs-4"] [
      div [class "panel panel-default "]  [
         div [class "panel-heading", style [("background", rgb)]] [(text repo.name)]
       , div [class "panel-body"] [(text ("url: " ++ repo.url))]
     ]
   ]

view : Signal.Address Action -> Model -> Html
view address ({repos} as model) = 
  div  [class "container-fluid"] 
    (List.map (\pair ->  div [class "row"] pair) (partition 3 (List.map repoPanel repos)))
  

repo : Decoder Repository
repo = 
  object3 Repository
    ("url" := string)
    ("name" := string)
    ("status-changed" := bool)

 
repos : Decoder (List Repository)
repos = 
 at ["repos"] (list repo)
  

getRepos: Effects Action
getRepos = 
  Http.get repos ("/repos/list")
    |> Task.toResult
    |> Task.map Load
    |> Effects.task

