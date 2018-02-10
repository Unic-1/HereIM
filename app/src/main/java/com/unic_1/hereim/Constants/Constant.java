package com.unic_1.hereim.Constants;

import com.unic_1.hereim.Model.NotificationModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by unic-1 on 25/8/17.
 */

public class Constant {

    // Types of actions in a request
    public enum Actions {
        REQUEST_SENT(0),
        REQUEST_RECEIVED(1),
        LOCATION_RECEIVED(3),
        REQEUST_DECLINED(4),
        LOCATION_SENT(2);

        public int value;
        private Actions(int value) {
            this.value = value;
        }
    }

    public static final String[] COUNTRYLIST = new String[] {"Abkhazia", "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antigua and Barbuda", "Argentina Republic", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia & Herzegov.", "Botswana", "Brazil", "British Virgin Islands", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Rep.", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo, Dem. Rep.", "Congo, Republic", "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Curacao", "Cyprus", "Czech Rep.", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "French Guiana", "French Polynesia", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe ", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hongkong, China", "Hungary", "Iceland", "India", "Indonesia", "International Networks", "Iran ", "Iraq", "Ireland", "Israel", "Italy", "Ivory Coast", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea N., Dem. People's Rep.", "Korea S, Republic of", "Kuwait", "Kyrgyzstan", "Laos P.D.R.", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macao, China", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Martinique (French Department of)", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Myanmar (Burma)", "Namibia", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norway", "Oman", "Pakistan", "Palau (Republic of)", "Palestinian Territory", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Samoa", "San Marino", "Sao Tome & Principe", "Satellite Networks", "Saudi Arabia", "Senegal", "Serbia ", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "Spain", "Sri Lanka", "St. Pierre & Miquelon", "St. Vincent & Gren.", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Viet Nam", "Virgin Islands, U.S.", "Yemen", "Zambia", "Zimbabwe"};
    public static final String[] COUNTRYCODE = new String[] {"7", "93", "355", "213", "684", "376", "244", "1264", "1268", "54", "374", "297", "61", "43", "994", "1242", "973", "880", "1246", "375", "32", "501", "229", "1441", "975", "591", "387", "267", "55", "284", "673", "359", "226", "257", "855", "237", "1", "238", "1345", "236", "235", "56", "86", "57", "269", "243", "242", "682", "506", "385", "53", "599", "357", "420", "45", "253", "1767", "1809", "593", "20", "503", "240", "291", "372", "251", "500", "298", "679", "358", "33", "594", "689", "241", "220", "995", "49", "233", "350", "30", "299", "1473", "590", "1671", "502", "224", "245", "592", "509", "504", "852", "36", "354", "91", "62", "882", "98", "964", "353", "972", "39", "225", "1876", "81", "962", "7", "254", "686", "850", "82", "965", "996", "856", "371", "961", "266", "231", "218", "423", "370", "352", "853", "389", "261", "265", "60", "960", "223", "356", "596", "222", "230", "52", "691", "373", "377", "976", "382", "1664", "212", "258", "95", "264", "977", "599", "31", "687", "64", "505", "227", "234", "683", "47", "968", "92", "680", "970", "507", "675", "595", "51", "63", "48", "351", "974", "262", "40", "79", "250", "1869", "1758", "685", "378", "239", "870", "966", "221", "381", "248", "232", "65", "421", "386", "677", "252", "27", "34", "94", "508", "1784", "249", "597", "268", "46", "41", "963", "886", "992", "255", "66", "670", "228", "676", "1868", "216", "90", "993", "256", "380", "971", "44", "1", "598", "998", "678", "58", "84", "1340", "967", "260", "263"};

    public static ArrayList<NotificationModel> getList() {
        ArrayList<NotificationModel> notificationList = new ArrayList<>();

        notificationList.add(new NotificationModel(
                "Hey Manoj! where are you?",
                "Satyam"
        ));
        notificationList.add(new NotificationModel(
                "Hey Satyadeep! where are you?",
                "Amit"
        ));
        notificationList.add(new NotificationModel(
                "Hey Satyadeep! where are you?",
                "Satyam"
        ));

        return notificationList;
    }

    private HashMap<String, String> countryCodes = new HashMap<>();


}
