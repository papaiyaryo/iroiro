import geopandas as gpd
import matplotlib.pyplot as plt
import pandas as pd
import glob

# 日本のシェープファイルを読み込み
japan = gpd.read_file("jpnShapefile/jpn_1.shp")

data = pd.read_csv('done.csv')

df = pd.DataFrame(data)

# シェープファイルとデータをマージ
merged = japan.set_index('NAME_1').join(df.set_index('Prefecture'))

# テキストファイルを読み込み
geocode_result = pd.read_csv('\\Takeout\\want.txt', sep='\n', header=None)

# 行きたい場所の緯度経度を取得

locations = []
try:
    for item in geocode_result[0]:
        if item and 'geometry' in item[0] and 'location' in item[0]['geometry']:
            location = item[0]['geometry']['location']
            locations.append((location['lat'], location['lng']))
except (IndexError, KeyError) as e:
    print(f"Error: {e}")

    #日本地図に表示する
fig, ax = plt.subplots(1, 1, figsize=(10, 15))
japan.boundary.plot(ax=ax)
for i in range(len(locations)):
    plt.plot(locations[i][1], locations[i][0], 'ro')
merged.plot(column='state', ax=ax, legend=True,
            legend_kwds={'label': "state",
                         'orientation': "horizontal"})
plt.title('Prefectures I have been to and places I want to go to')
plt.show()
