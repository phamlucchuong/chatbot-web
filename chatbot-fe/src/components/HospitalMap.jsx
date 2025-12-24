import React, { useEffect, useRef, useState } from 'react';
import { MapPin, Navigation } from 'lucide-react';

const HospitalMap = ({ hospitals = [] }) => {
  const [map, setMap] = useState(null);
  const [leafletLoaded, setLeafletLoaded] = useState(false);
  const [selectedHospital, setSelectedHospital] = useState(null);
  const mapRef = useRef(null);
  const markersRef = useRef([]);

  // Load Leaflet library
  useEffect(() => {
    if (window.L) {
      setLeafletLoaded(true);
      return;
    }

    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.css';
    document.head.appendChild(link);

    const script = document.createElement('script');
    script.src = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.js';
    script.onload = () => setLeafletLoaded(true);
    document.body.appendChild(script);

    return () => {
      if (document.head.contains(link)) document.head.removeChild(link);
      if (document.body.contains(script)) document.body.removeChild(script);
    };
  }, []);

  // Khởi tạo bản đồ
  useEffect(() => {
    if (!leafletLoaded || !mapRef.current || map) return;

    const L = window.L;
    
    // Tính center từ danh sách bệnh viện hoặc dùng TP.HCM mặc định
    let center = [10.8231, 106.6297];
    if (hospitals.length > 0) {
      const avgLat = hospitals.reduce((sum, h) => sum + h.lat, 0) / hospitals.length;
      const avgLng = hospitals.reduce((sum, h) => sum + h.lng, 0) / hospitals.length;
      center = [avgLat, avgLng];
    }

    const mapInstance = L.map(mapRef.current).setView(center, 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(mapInstance);

    setMap(mapInstance);

    return () => {
      if (mapInstance) {
        mapInstance.remove();
      }
    };
  }, [leafletLoaded, hospitals]);

  // Thêm markers cho các bệnh viện
  useEffect(() => {
    if (!map || !window.L || hospitals.length === 0) return;

    const L = window.L;

    // Xóa markers cũ
    markersRef.current.forEach(marker => {
      try {
        marker.remove();
      } catch (e) {
        console.error('Error removing marker:', e);
      }
    });
    markersRef.current = [];

    // Thêm markers mới
    hospitals.forEach((hospital, index) => {
      const marker = L.marker([hospital.lat, hospital.lng], {
        icon: L.divIcon({
          className: 'hospital-marker',
          html: `<div style="background: #ef4444; width: 28px; height: 28px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; color: white; font-size: 12px; font-weight: bold;">${index + 1}</div>`,
          iconSize: [28, 28]
        })
      })
        .addTo(map)
        .bindPopup(`<strong>${hospital.name}</strong>`)
        .on('click', () => {
          setSelectedHospital(hospital);
          map.setView([hospital.lat, hospital.lng], 15);
        });

      markersRef.current.push(marker);
    });

    // Fit bounds để hiển thị tất cả markers
    if (hospitals.length > 0) {
      const bounds = L.latLngBounds(hospitals.map(h => [h.lat, h.lng]));
      map.fitBounds(bounds, { padding: [30, 30] });
    }
  }, [map, hospitals]);

  // Mở Google Maps
  const openGoogleMaps = (hospital) => {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${hospital.lat},${hospital.lng}`;
    window.open(url, '_blank');
  };

  // Loading state
  if (!leafletLoaded) {
    return (
      <div className="w-full h-64 flex items-center justify-center bg-gray-50 rounded-lg">
        <div className="text-center">
          <div className="w-6 h-6 border-3 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
          <p className="text-xs text-gray-600">Đang tải bản đồ...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full bg-white rounded-lg overflow-hidden border border-gray-200">
      {/* Bản đồ */}
      <div className="relative" style={{ height: '300px' }}>
        <div ref={mapRef} className="w-full h-full" />
        
        {/* Badge số lượng bệnh viện */}
        {hospitals.length > 0 && (
          <div className="absolute top-2 left-2 bg-white px-2 py-1 rounded-full shadow-md z-[400] flex items-center gap-1.5">
            <MapPin size={14} className="text-red-500" />
            <span className="text-xs font-semibold text-gray-700">
              {hospitals.length} bệnh viện
            </span>
          </div>
        )}
      </div>

      {/* Danh sách bệnh viện */}
      {hospitals.length > 0 && (
        <div className="max-h-48 overflow-y-auto border-t">
          {hospitals.map((hospital, index) => (
            <div
              key={index}
              className={`p-2.5 border-b last:border-b-0 cursor-pointer transition-colors ${
                selectedHospital === hospital ? 'bg-blue-50' : 'hover:bg-gray-50'
              }`}
              onClick={() => {
                setSelectedHospital(hospital);
                if (map) {
                  map.setView([hospital.lat, hospital.lng], 15);
                }
              }}
            >
              <div className="flex items-start gap-2.5">
                {/* Số thứ tự */}
                <div className="w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center flex-shrink-0 text-xs font-bold">
                  {index + 1}
                </div>
                
                {/* Thông tin bệnh viện */}
                <div className="flex-1 min-w-0">
                  <h4 className="font-semibold text-gray-800 text-sm mb-0.5">
                    {hospital.name}
                  </h4>
                  <p className="text-xs text-gray-500">
                    {hospital.lat.toFixed(6)}, {hospital.lng.toFixed(6)}
                  </p>
                </div>

                {/* Nút chỉ đường */}
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    openGoogleMaps(hospital);
                  }}
                  className="flex items-center gap-1 px-2 py-1 text-xs bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors flex-shrink-0"
                >
                  <Navigation size={12} />
                  Chỉ đường
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Thông báo khi không có dữ liệu */}
      {hospitals.length === 0 && (
        <div className="p-6 text-center text-gray-500">
          <MapPin size={32} className="mx-auto mb-2 text-gray-300" />
          <p className="text-xs">Không có dữ liệu bệnh viện</p>
        </div>
      )}
    </div>
  );
};

export default HospitalMap;