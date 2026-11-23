# AstraxMenu 2.0 (AstraxBox)

Plugin menu GUI cho Paper 1.21.4: menu hướng dẫn, sách, xem quyền lợi rank và kit.
Bản 2.0 viết lại toàn bộ, thêm **model** cho vật phẩm trong menu và dùng 6 rank mới
(Pro, Platinum, Ruby, Trial, Tejo, Apex) với emblem động.

## Build trên GitHub (không cần cài gì)
Cấu trúc "flat": mọi file `.java` và `.yml` nằm ở thư mục gốc cùng `pom.xml`.
1. Tạo repo trên GitHub, upload **toàn bộ** nội dung (nhớ cả thư mục `.github`).
2. Vào tab **Actions** → workflow *Build AstraxMenu* tự chạy (hoặc bấm *Run workflow*).
3. Chạy xong, mở lần chạy đó → mục **Artifacts** → tải `AstraxMenu` → giải nén ra `AstraxMenu-2.0.0.jar`.
Workflow có bước kiểm tra jar phải chứa `plugin.yml` và các file cấu hình, thiếu là báo lỗi đỏ.

Build trên máy: JDK 21 + Maven, chạy `mvn clean package` → `target/AstraxMenu-2.0.0.jar`.

## Cài đặt
1. Cài phần resource pack trong `AstraxBox_Ranks_Final.zip` (thư mục `Oraxen/items/rank_models.yml`
   và `Oraxen/pack/textures/ranks/item/`), rồi `/oraxen reload all`, thoát hẳn game vào lại.
2. Bỏ jar vào `plugins/`, khởi động. Plugin tự tạo `config.yml`, `menus.yml`, `ranks.yml`, `guides.yml`, `messages.yml`.
3. Sửa `ranks.yml` (giá, quyền lợi, tên kit) rồi `/astraxmenu reload`.

## Model trong menu (mới)
Trong `menus.yml` / `config.yml` mỗi vật phẩm có thể dùng:

    material: PAPER
    model: oraxen:rank_pro       # item_model của resource pack (giống "model: oraxen:onyx" trong kit)
    # oraxen: rank_pro            # hoặc lấy thẳng vật phẩm Oraxen theo id
    # custom-model-data: 1001     # hoặc CustomModelData số

`{rank_model}` = model của rank cao nhất người chơi đang có (nút Rank ở menu chính dùng biến này).

## Rank
Thứ tự thấp → cao: pro, platinum, ruby, trial, tejo, apex. Rank Pro lấy theo kit thật của bạn;
5 rank còn lại là **mẫu**. Quyền để coi là "có rank" mặc định `group.<id>` (LuckPerms).
Kit đọc từ `plugins/PlayerKits2/kits/<kit>.yml` (chỉ đọc). Bấm chuột phải để nhận kit (chạy `/kit <tên>`).

## Lệnh
- `/menu [tên]` (alias /huongdan /guide /bxmenu), `/rank [list|id]`, `/astraxmenu reload|list|check|open <menu> [người]`
- PlaceholderAPI: `%astraxmenu_rank%`, `_rank_id`, `_rank_label`, `_rank_glyph`, `_rank_model`

## Hành động trong menu
`[player] lệnh`, `[console] lệnh`, `[open] menu`, `[rank] [id]`, `[book] id`, `[close]`, `[message] chữ`, `[sound] key`, `[link] url | chữ`

## Shop key (mua bằng poin)
Menu `shop_key` (nút ở menu chính). Cần plugin **PlayerPoints**. Mỗi key là 1 nút trong `menus.yml`:
`[cost-points] giá` trừ poin (không đủ thì dừng), sau đó `[console]` phát key. Lệnh mặc định:
`crate key give {player} sea_key 1` (ExcellentCrates). Nếu lệnh phát key khác, sửa trong `menus.yml` và test 1 lần mua.
Biến `{points}` hiện số poin của người chơi.

## Rank
Icon rank dùng vật phẩm thường (quặng vàng, quặng kim cương, khối vàng/kim cương), đổi ở `icon:` trong `ranks.yml`.
Muốn dùng model riêng thì thêm `model: oraxen:xxx` cho rank đó.
