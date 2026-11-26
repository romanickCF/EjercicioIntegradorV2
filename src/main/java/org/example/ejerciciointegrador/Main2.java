package org.example.ejerciciointegrador;

import org.example.ejerciciointegrador.data.DataBaseInitializer;
import org.example.ejerciciointegrador.data.DataSourceFactory;

public class Main2 {
     public static void main(){

          DataBaseInitializer.initialize(DataSourceFactory.getDataSource());
          DataBaseInitializer.loadData(DataSourceFactory.getDataSource());



     }


}
