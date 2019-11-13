function onMessage(evt) {

    pos = $.parseJSON(evt.data);

    if (pos.pollResult === true) {
        console.log(pos);
    }

    if (pos.oilId === undefined) {


        if (window.map.ships[pos.imoNumber] === undefined) {
            var iconFeature = new ol.Feature({
                geometry: new ol.geom.Point(ol.proj.transform([pos.position.longitude,pos.position.latitude], 'EPSG:4326','EPSG:3857'))
            });

            iconFeature.imoNumber = pos.imoNumber;
            var ship = new Ship(pos.imoNumber,iconFeature);
            window.shipLayer.getSource().addFeatures([iconFeature]);
            window.map.ships[pos.imoNumber] = ship;
        } else {
            window.map.ships[pos.imoNumber].marker.getGeometry().setCoordinates(ol.proj.transform([pos.position.longitude,pos.position.latitude], 'EPSG:4326','EPSG:3857'));
        }

    } else {

        var format = new ol.format.WKT();

        var f1 = format.readFeature(pos.shape, {
             dataProjection: 'EPSG:4326',
             featureProjection: 'EPSG:3857'
        });


        if (window.map.oil[pos.oilId] === undefined) {
            var iconFeature = f1

            iconFeature.oilId = pos.oilId;
            var oil = new Ship(pos.oilId,iconFeature);
            window.oilLayer.getSource().addFeatures([iconFeature]);
            window.map.oil[pos.oilId] = oil;
        } else {
            window.map.oil[pos.oilId].marker.setGeometry(f1.getGeometry());
        }


    }
}


function addArea(simArea) {

    var format = new ol.format.WKT();
    
    var f1 = format.readFeature(simArea, {
             dataProjection: 'EPSG:4326',
             featureProjection: 'EPSG:3857'
             });

    var polygonVectorSource = new ol.source.Vector({
      features: [f1]
    });

    var areaLayer = new ol.layer.Vector({
      source: polygonVectorSource,
      style: new ol.style.Style({
//          fill: new ol.style.Fill({
//            color: 'rgba(255, 69, 0, 0.7)'
//          }),
          stroke: new ol.style.Stroke({
            color: 'rgba(255, 69, 0, 0.9)',
            width: 2
          })
      })
    });

    window.map.addLayer(areaLayer);
}

function loadMap() {
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
      style: function(feature) {
        shipStyle.getText().setText((resolution < 5000 ? feature.imoNumber : ''));
        return shipStyle;
      }
    });


    window.shipLayer = new ol.layer.Vector({
      source: window.shipImageSource,
      style: function(feature) {
        shipStyle.getText().setText(feature.imoNumber);
        return shipStyle;
      }
    });

        window.oilSource = new ol.source.Vector();

        window.oilImageSource = new ol.source.Vector({
          source: window.oilSource,
      style: new ol.style.Style({
          fill: new ol.style.Fill({
            color: 'rgba(0, 0, 0, 0.7)'
          }),
          stroke: new ol.style.Stroke({
            color: 'rgba(255, 255, 255, 1.0)',
            width: 2
          })
      })
        });

            window.oilLayer = new ol.layer.Vector({
              source: window.oilImageSource,
                    style: new ol.style.Style({
                             fill: new ol.style.Fill({
                               color: 'rgba(0, 0, 0, 0.7)'
                             }),
                             stroke: new ol.style.Stroke({
                               color: 'rgba(255, 255, 255, 1.0)',
                               width: 2
                             })
                         })
            });




    var map = new ol.Map({
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM()
        })
      ],
      target: 'map',
      controls: ol.control.defaults({
        attributionOptions: ({
          collapsible: false
        })
      }),
      view: new ol.View({
        center: [0, 0],
        center: [0, 0],
        zoom: 2
      })
    });

    window.map = map;
    window.map.ships = {};
    window.map.oil = {};

    map.addLayer(window.shipLayer);
    map.addLayer(window.oilLayer);
}