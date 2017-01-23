# nstyle
Макет приложения для android, иллюстрирующий обработку картинок через разработанный серверный API, который описан в [shipilov.name.nstyle.api.ServerApi](https://github.com/ilyashipilov/nstyle/blob/master/NStylePrototype/src/main/java/shipilov/name/nstyle/api/ServerApi.java), для работы с сервером используется библиотека Retrofit2. 

Параметры конфигурации находятся в [assets/config.properties](https://github.com/ilyashipilov/nstyle/blob/master/NStylePrototype/src/main/assets/config.properties):
- url сервера (при тестировании на виртуальном сервере amazon после перезапуска инстанса IP меняется, приходится пересобирать);
- размер отправляемого на сервер изображения;
- качество jpeg отправляемого изображения.

Тестировал на Lenovo A1000 - Android версии 5 - работает. 

Для предотвращения использования сервера посторонними приложениями необходимо обфуцировать класс, отвечающий за расчет хэша запроса - [shipilov.name.nstyle.HashCalculator](https://github.com/ilyashipilov/nstyle/blob/master/NStylePrototype/src/main/java/shipilov/name/nstyle/HashCalculator.java)
