/*
 *  Copyright 2010 Richard Nichols.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.visural.common.web.api.googlecharts;

/**
 * @version $Id: ImageIcons.java 31 2010-05-21 07:15:23Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public enum ImageIcons {

    academy,
    activities,
    airport,
    amusement,
    aquarium,
    art_gallery,
    atm,
    baby,
    bank_dollar,
    bank_euro,
    bank_intl,
    bank_pound,
    bank_yen,
    bar,
    barber,
    beach,
    beer,
    bicycle,
    books,
    bowling,
    bus,
    cafe,
    camping,
    car_dealer,
    car_rental,
    car_repair,
    casino,
    caution,
    cemetery_grave,
    cemetery_tomb,
    cinema,
    civic_building,
    computer,
    corporate,
    courthouse,
    fire,
    flag,
    floral,
    helicopter,
    home,
    info,
    landslide,
    legal,
    location,
    locomotive,
    medical,
    mobile,
    motorcycle,
    music,
    parking,
    pet,
    petrol,
    phone,
    picnic,
    postal,
    repair,
    restaurant,
    sail,
    school,
    scissors,
    ship,
    shoppingbag,
    shoppingcart,
    ski,
    snack,
    snow,
    sport,
    star,
    swim,
    taxi,
    train,
    truck,
    wc_female,
    wc_male,
    wc,
    wheelchair;
   
    public String getIconName() {
        return name().replace('_', '-');
    }
}
