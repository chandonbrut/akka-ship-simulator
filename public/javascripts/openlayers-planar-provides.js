function onMessage(evt) {
    pos = $.parseJSON(evt.data);

    if (window.map.ships[pos.imoNumber] === undefined) {
        var iconFeature = new ol.Feature({
//            geometry: new ol.geom.Point(ol.proj.transform([pos.position.longitude,pos.position.latitude], 'EPSG:4326','EPSG:3857'))
            geometry: new ol.geom.Point(ol.proj.transform([pos.position.longitude,pos.position.latitude], 'EPSG:4326','ESRI:102016'))
        });

        iconFeature.imoNumber = pos.imoNumber;
        var ship = new Ship(pos.imoNumber,iconFeature);
        window.shipSource.addFeatures([iconFeature]);
        window.map.ships[pos.imoNumber] = ship;
    } else {
//        window.map.ships[pos.imoNumber].marker.getGeometry().setCoordinates(ol.proj.transform([pos.position.longitude,pos.position.latitude], 'EPSG:4326','EPSG:3857'));
        window.map.ships[pos.imoNumber].marker.getGeometry().setCoordinates(ol.proj.transform([pos.position.longitude,pos.position.latitude], 'EPSG:4326','ESRI:102016'));
    }
}


function addArea(simArea) {

    var format = new ol.format.WKT();
    
    var f1 = format.readFeature(simArea, {
             dataProjection: 'EPSG:4326',
             featureProjection: 'ESRI:102016'
             //featureProjection: 'EPSG:3857'
             });

    var polygonVectorSource = new ol.source.Vector({
      features: [f1]
    });

    var areaLayer = new ol.layer.Vector({
      source: polygonVectorSource,
      style: new ol.style.Style({
          fill: new ol.style.Fill({
            color: 'rgba(255, 69, 0, 0.7)'
          }),
          stroke: new ol.style.Stroke({
            color: 'rgba(255, 69, 0, 0.9)',
            width: 1
          })
      })
    });

    window.map.addLayer(areaLayer);
}

function loadMap() {

    console.info("Defining proj!");
    proj4.defs('ESRI:102016','+proj=aeqd +lat_0=90 +lon_0=0 +x_0=0 +y_0=0 +ellps=WGS84 +datum=WGS84 +units=m +no_defs');
    ol.proj.proj4.register(proj4);

    var geojson_layer = new ol.layer.Vector({
        source: new ol.source.Vector({
            format: new ol.format.GeoJSON(),
            url: 'assets/javascripts/countries.geo.json',
             dataProjection: 'EPSG:4326',
             featureProjection: 'ESRI:102016'
        })
    });

     var shipStyle = new ol.style.Style({
            image: new ol.style.Icon(({
                anchor: [0,0],
                anchorXUnits: 'pixels',
                anchorYUnits: 'pixels',
                opacity: 0.75,
                src: 'assets/leaflet/images/marker-icon.png'
            })),
            text: new ol.style.Text({
                text: 'Z',
                offsetY: -25,
                fill: new ol.style.Fill({ color: '#fff' })
            })
        });

    var styles = [shipStyle];


    window.shipSource = new ol.source.Vector();

    window.shipImageSource = new ol.source.Vector({
      source: window.shipSource,
      style: function(feature,resolution) {
        shipStyle.getText().setText((resolution < 5000 ? feature.imoNumber : ''));
        return styles;
      }
    });


    window.shipLayer = new ol.layer.Vector({
      source: window.shipImageSource,
      style: function(feature,resolution) {
        shipStyle.getText().setText(feature.imoNumber);
        return styles;
      }
    });

    var map = new ol.Map({
       layers: [
             geojson_layer
            ],
      target: 'map',
      controls: ol.control.defaults({
        attributionOptions: ({
          collapsible: false
        })
      }),
      view: new ol.View({
        center: [0, 0],
//        center: [0, 0],
        zoom: 2,
        projection: 'ESRI:102016'
      })
    });

    window.map = map;
    window.map.ships = {};

    map.addLayer(window.shipLayer);
}
