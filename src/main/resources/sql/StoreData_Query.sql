select t_storeList.*,
       si_ItemID
from (
         select StoreID as sl_StoreID
         from Item_StoreList
     ) as t_storeList
         LEFT JOIN
     (
         select StoreID as sl_StoreID,
                ItemID  as si_ItemID
         from Item_Store_Items
     ) as t_storeItems
     ON t_storeList.sl_StoreID = t_storeItems.sl_StoreID