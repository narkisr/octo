module Common.Utils where

import Effects exposing (Effects, Never, batch, map)

partition n list =
  let
    catch = (List.take n list)
  in 
    if n == (List.length catch) then
      [catch] ++ (partition n (List.drop n list))
    else
      [catch]

withDefaultProp :  Maybe a -> b -> (a -> b) -> b
withDefaultProp parent default prop = 
 case parent of
   Just v -> 
     (prop v)
   Nothing ->
     default
 

defaultEmpty : Maybe (List a) -> List a
defaultEmpty list =
  case list of
    Just result  ->
      result
    Nothing -> 
      []

none : a -> (a, Effects b)
none a =
  (a, Effects.none)
