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
 * @version $Id: ChartParams.java 31 2010-05-21 07:15:23Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public enum ChartParams {

    chart_type("cht"),
    chart_data("chd"),                  //chd=t:val,val,val|val,val,val...
    chart_title("chtt"),                //chtt=<chart_title>
    chart_title_color_size("chts"),     //chts=<color>,<font_size>
    pie_labels("chl"),                  //chl=<label_value>|...<label_value>
    pie_rotation("chp"),                //chp=<radians>
    chart_size("chs"),                  //chs=250x100
    chart_legend("chdl"),               //chdl=<data_series_1_label>|...|<data_series_n_label>
    chart_legend_color_size("chdlp"),   //chdlp=<position>|<label_order>
    chart_margins("chma"),              //chma=<left_margin>,<right_margin>,<top_margin>,<bottom_margin>|<legend_width>,<legend_height>
    visible_axes("chxt"),               //chxt=<axis_1>,...,<axis_n>     *  x - Bottom x-axis    * t - Top x-axis [Not supported by Google-o-Meter]    * y - Left y-axis     * r - Right y-axis [Not supported by Google-o-Meter]
    axis_range("chxr"),                 //chxr=<axis_index>,<start_val>,<end_val>,<step>|...|<axis_index>,<start_val>,<end_val>,<step>
    custom_axis_labels("chxl"),         //chxl=<axis_index>:|<label_1>|...|<label_n>|...|<axis_index>:|<label_1>|...|<label_n>
    axis_positions("chxp"),             //chxp=<axis_1_index>,<label_1_position>,...,<label_n_position> |...|<axis_m_index>,<label_1_position>,...,<label_n_position>
    axis_label_styles("chxs"),          //chxs=<axis_index><optional_format_string>,<label_color>,<font_size>,<alignment>,<axis_or_tick>,<tick_color>|...|<axis_index><optional_format_string>,<label_color>,<font_size>,<alignment>,<axis_or_tick>,<tick_color>
    axis_tick_mark_styles("chxtc"),     //chxtc=<axis_index_1>,<tick_length_1>,...,<tick_length_n>|...|<axis_index_m>,<tick_length_1>,...,<tick_length_n>
    background_fills("chf"),             //chf=<fill_type>,s,<color>|...
                                        //   *  bg - Background fill     * c - Chart area fill. Not supported for map charts.       * a - Make the whole chart (including backgrounds) transparent. The first six digits of <color> are ignored, and only the last two (the transparency value) are applied to the whole chart and all fills.
                                        //   * b<index> - Bar solid fills (bar charts only). Replace <index> with the series index of the bars to fill with a solid color. The effect is similar to specifying chco in a bar chart. See Bar Chart Series Colors for an example.
                                        // s :  Indicates a solid or transparency fill. <color>    The fill color, in RRGGBB hexadecimal format. For transparencies, the first six digits are ignored, but must be included anyway.
                                        //gradient fills: chf=<fill_type>,lg,<angle>,<color_1>,<color_centerpoint_1>,...,<color_n>,<color_centerpoint_n>
                                        //striped fills: chf=<fill_type>,ls,<angle>,<color_1>,<width_1> ,...,<color_n>,<width_n>
    grid_lines("chg"),                  //chg=<x_axis_step_size>,<y_axis_step_size>,<dash_length>,<space_length>,<x_offset>,<y_offset>
    series_colors("chco"),              //chco=<series_1_color>, ..., <series_n_color>   or  chco=<series_1_bar_1>|<series_1_bar_2>|...|<series_1_bar_n>,<series_2>,...,<series_n>
    bar_width_spacing("chbh"),          //chbh=<bar_width_or_scale>,<space_between_bars>,<space_between_groups>
    zero_lines("chp")                   //chp=<zero_value_series_1>,...,<zero_value_series_n>

            ;

    ChartParams(String id) {
        this.id = id;
    }
    private final String id;

    public String getId() {
        return id;
    }
}
