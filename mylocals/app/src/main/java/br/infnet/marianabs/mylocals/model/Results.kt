package br.infnet.marianabs.mylocals.model

import br.infnet.marianabs.mylocals.model.Geometry
import br.infnet.marianabs.mylocals.model.OpeningHours
import br.infnet.marianabs.mylocals.model.Photos

class Results {
    var opening_hours: OpeningHours?=null
    var place_id:String?=null
    var price_level:Int=0
    var rating:Double=0.0
    var reference:String?=null
    var types:Array<String>?=null
    var vicinity:String?=null
    var scope:String?=null
    var name:String?=null
    var icon:String?=null
    var geometry: Geometry?=null
    var photos:Array<Photos>?=null
    var id:String?=null
}